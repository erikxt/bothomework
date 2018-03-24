# bothomework
基于知识库的问答机器人

使用说明
------
### 系统要求
安装有 Jdk 8+, Maven 3.3+, Docker, Git的Linux发行版
### 下载项目
从github拉取工程，并进入工作目录
```
git clone https://github.com/erikxt/bothomework.git
cd bothomework
```
### 启动redis容器，开启持久化
```
docker run --name some-redis -p "6379:6379" -d redis redis-server --appendonly yes
```
### 构建docker镜像
```
mvn clean package docker:build
```
### 运行docker镜像
```
docker run -p 8080:8080 bot-homework
```