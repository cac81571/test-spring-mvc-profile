# test-spring-mvc-profile

Spring MVC（Spring Boot なし）で、環境変数 `APP_ENV` によって CSS フォルダー、Logback の設定ファイル、プロパティファイルが切り替わるかを確かめるサンプルです。

- JDK 21 / Maven / Spring Framework 6.2.11 / Jakarta Servlet 6.0 / Logback 1.5.18
- JBoss EAP 8.1（Jakarta EE 10）向け。web.xml は使いません

## 構成

```
pom.xml
src/main/java/com/example/config/AppInitializer.java   # DispatcherServlet を "/" に登録
src/main/java/com/example/config/WebConfig.java        # 検証対象の設定
src/main/java/com/example/web/HomeController.java      # "/" で画面を表示
src/main/webapp/WEB-INF/views/home.jsp
src/main/webapp/static/common.css                      # 全環境共通。CSS 変数を参照する
src/main/webapp/dev/env.css                            # 水色の背景と "DEV"
src/main/webapp/prod/env.css                           # ピンクの背景と "PROD"
src/main/resources/logback.xml                         # APP_ENV に応じた設定を include する
src/main/resources/logback-dev.xml                     # com.example と root は DEBUG
src/main/resources/logback-prod.xml                    # com.example と root は INFO
src/main/resources/application-dev.properties          # dev 用。app.message は APP_MESSAGE で上書きできる
src/main/resources/application-prod.properties         # prod 用。同上
src/main/webapp/WEB-INF/jboss-deployment-structure.xml # WAR 内の Logback を使う
```

## ビルド

```
mvn clean package
```

`target/test-spring-mvc-profile.war` ができます。

## EAP 8.1 へのデプロイ

方法1：WAR をコピーする

```
copy target\test-spring-mvc-profile.war %JBOSS_HOME%\standalone\deployments\
```

方法2：jboss-cli を使う（EAP を起動した状態で）

```
%JBOSS_HOME%\bin\jboss-cli.bat --connect --command="deploy target\test-spring-mvc-profile.war --force"
```

## APP_ENV の指定

指定しないときは `prod` になります。

システムプロパティで指定する場合：

```
standalone.bat -DAPP_ENV=dev
```

環境変数で指定する場合：

```
set APP_ENV=dev
standalone.bat
```

`PropertySourcesPlaceholderConfigurer` が `Environment` から `${APP_ENV:prod}` を解決します。システムプロパティと環境変数の両方を指定した場合は、システムプロパティが優先されます。

OpenShift では `oc set env deployment/<eap-app> APP_ENV=dev` で指定できます。

## ログレベル

`logback.xml` は `logback-${APP_ENV:-prod}.xml` を読みます。未設定のときは `logback-prod.xml` です。root のレベルもそのファイルで決まります。

| APP_ENV | com.example | root | 画面の DEBUG |
|---|---|---|---|
| なし / prod | INFO | INFO | 無効 |
| dev | DEBUG | DEBUG | 有効 |

`/` を開くと `app-env-log-check` を DEBUG と INFO で出力します。prod では INFO だけが EAP のログに出ます。EAP 標準の SLF4J を外して WAR 内の Logback を使うため、`jboss-deployment-structure.xml` で `org.slf4j` と `org.slf4j.impl` を除外しています。どのファイルを読むかも起動時に1回だけ決まります。

## プロパティファイル

`application-${APP_ENV}.properties` を読みます。未設定のときは `application-prod.properties` です。ファイルの選択は `PropertySourcesPlaceholderConfigurer` を作る時点で行い、システムプロパティの `APP_ENV` を環境変数より優先します。

| キー | dev | prod |
|---|---|---|
| app.profile.label | DEV | PROD |
| app.message | `${APP_MESSAGE:これは dev のプロパティです}` | `${APP_MESSAGE:これは prod のプロパティです}` |

`app.message` は、未設定ならファイルに書いた既定値（`${APP_MESSAGE:既定値}` のコロン以降）になります。環境変数 `APP_MESSAGE` を指定すると、その値に変わります。Spring は環境変数名の `.` と `_` を同じものとして扱い、大文字小文字も区別しないため、`APP_MESSAGE` はキー `app.message` に対応します。

同じキーをシステムプロパティで上書きするときは `-Dapp.message=...` です。システムプロパティは環境変数より優先されます。`-DAPP_MESSAGE=...` は、環境変数 `APP_MESSAGE` が無いときだけ、ファイル内のプレースホルダから読まれます。

`app.profile.label` のようにプレースホルダの無い値も、同じキーのシステムプロパティ（`-Dapp.profile.label=...`）があればそちらが使われます。プロパティファイル側を優先したい場合は `setLocalOverride(true)` にします。このサンプルでは既定のまま（Environment が優先）です。

画面の `app.profile.label` と `app.message` で、ファイルの切り替えと環境変数による上書きを確認できます。

## 確認する URL

- http://localhost:8080/test-spring-mvc-profile/
- http://localhost:8080/test-spring-mvc-profile/static/env.css
- http://localhost:8080/test-spring-mvc-profile/static/common.css

| 指定 | 画面 | env.css の中身 |
|---|---|---|
| なし | ピンクの背景に "PROD"、APP_ENV は prod | `/* PROD */` |
| dev | 水色の背景に "DEV"、APP_ENV は dev | `/* DEV */` |

画面は `common.css` だけを読み込みます。`common.css` 先頭の `@import url("env.css")` が、同じ `/static/env.css` をブラウザに要求します。色と表示文言は、その CSS 変数（`--env-bg`、`--env-label`、`--env-label-color`）で切り替わります。`@import` は他のルールより前に置きます。

画面には、`@Value` で読んだ値と、システムプロパティ・環境変数の生の値も表示されます。プロパティの確認例は次のとおりです。

```
set APP_ENV=dev
set APP_MESSAGE=環境変数で上書き
standalone.bat
```

このとき `app.profile.label` は `DEV`、`app.message` は `環境変数で上書き` になります。`APP_MESSAGE` を外して再起動すると、dev の既定メッセージに戻ります。

## 注意点

- フォルダーは起動時に1回だけ決まります。切り替えるときは EAP を再起動してください。
- `/{appEnv}/` を先に探し、無ければ `/static/` を探します。フォルダーが無い値を指定したときは、`env.css` は 404 になります。`common.css` は `/static/` 直下から配信されます。同じ相対パスが両方にあるときは、`APP_ENV` 側のファイルが使われます。
- ここで読んでいるのはプロパティの値だけです。`@Profile` で Bean を切り替えるのとは別の仕組みです。
