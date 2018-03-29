# bothomework
基于知识库的问答机器人，排序策略在不同权重的关键词下还有问题，有待调整

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
docker run --name some-redis -p 6379:6379 -d redis redis-server --appendonly yes
```
### 构建mysql镜像，初始化数据库表结构及数据，启动容器
```
docker build -t mydb ./mysql
docker run --name some-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -d mydb
```
### 构建docker镜像
```
mvn clean package docker:build
```
### 运行构建的应用容器，并链接到redis、mysql容器，退出后自动删除
```
docker run --name mywork -p 8080:8080 --link some-redis:redis --link some-mysql:mydb --rm bot-homework 
```
浏览器输入服务器IP:端口（8080）  访问应用