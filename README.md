# test-spring-mvc-profile

Spring MVC（Spring Boot なし）の `addResourceHandlers` で、環境変数 `APP_ENV` によって配信する CSS フォルダーが切り替わるかを確かめるサンプルです。

- JDK 21 / Maven / Spring Framework 6.2.11 / Jakarta Servlet 6.0
- JBoss EAP 8.1（Jakarta EE 10）向け。web.xml と jboss-deployment-structure.xml は使いません

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

## 確認する URL

- http://localhost:8080/test-spring-mvc-profile/
- http://localhost:8080/test-spring-mvc-profile/static/env.css
- http://localhost:8080/test-spring-mvc-profile/static/common.css

| 指定 | 画面 | env.css の中身 |
|---|---|---|
| なし | ピンクの背景に "PROD"、APP_ENV は prod | `/* PROD */` |
| dev | 水色の背景に "DEV"、APP_ENV は dev | `/* DEV */` |

画面は `common.css` だけを読み込みます。`common.css` 先頭の `@import url("env.css")` が、同じ `/static/env.css` をブラウザに要求します。色と表示文言は、その CSS 変数（`--env-bg`、`--env-label`、`--env-label-color`）で切り替わります。`@import` は他のルールより前に置きます。

画面には、`@Value` で読んだ値と、システムプロパティ・環境変数の生の値も表示されます。

## 注意点

- フォルダーは起動時に1回だけ決まります。切り替えるときは EAP を再起動してください。
- `/{appEnv}/` を先に探し、無ければ `/static/` を探します。フォルダーが無い値を指定したときは、`env.css` は 404 になります。`common.css` は `/static/` 直下から配信されます。同じ相対パスが両方にあるときは、`APP_ENV` 側のファイルが使われます。
- ここで読んでいるのはプロパティの値だけです。`@Profile` で Bean を切り替えるのとは別の仕組みです。
