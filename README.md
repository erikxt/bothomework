# bothomework
基于知识库的问答机器人


使用说明
------
### 系统要求
安装有 Jdk 8+, Maven 3.3+, Docker, Git的Linux发行版
### 下载项目
在Linux工作目录下执行命令
```
git clone https://github.com/erikxt/bothomework.git
```
### 构建docker镜像
```
cd bothomework
mvn clean package docker:build
```
### 运行docker镜像
```
docker run -p 8080:8080 bot-homework
```