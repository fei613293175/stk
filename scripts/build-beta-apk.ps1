$ErrorActionPreference = "Stop"
throw "本项目禁止在本机构建 APK。请连接已配置服务器，在服务器源码目录运行 scripts/build-beta-apk.sh。"
