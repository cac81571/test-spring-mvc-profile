<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>test-spring-mvc-profile</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/common.css">
</head>
<body>
  <h1>Spring MVC profile test</h1>
  <p>APP_ENV (@Value): <b>${appEnv}</b></p>
  <p>System property: ${empty sysProp ? '(未設定)' : sysProp}</p>
  <p>環境変数 APP_ENV: ${empty envVar ? '(未設定)' : envVar}</p>
  <p>app.profile.label: <b>${profileLabel}</b></p>
  <p>app.message: <b>${appMessage}</b></p>
  <p>System property APP_MESSAGE: ${empty messageSysProp ? '(未設定)' : messageSysProp}</p>
  <p>環境変数 APP_MESSAGE: ${empty messageEnvVar ? '(未設定)' : messageEnvVar}</p>
  <p>com.example の DEBUG: <b>${debugEnabled ? '有効' : '無効'}</b></p>
  <p>root のレベル: <b>${rootLevel}</b></p>
  <p><a href="${pageContext.request.contextPath}/static/env.css">/static/env.css を開く</a></p>
  <p><a href="${pageContext.request.contextPath}/static/common.css">/static/common.css を開く</a></p>
</body>
</html>
