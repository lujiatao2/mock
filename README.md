# 1 简介

&emsp;&emsp;Mock是一个零侵入的服务端Mock平台，底层基于JVM Sandbox。相比于Fiddler、Charles和Burp
Suite等客户端的代理调试工具，Mock的优势在于可以对调用链中的任何服务节点数据进行Mock，而不只是Mock离客户端最近一个节点的数据。

## 1.1 主要特性

* 零侵入，即不修改目标应用程序任何代码就能实现Mock功能。
* 支持静态Mock，即根据配置的类和方法，返回Mock数据。
* 支持动态Mock，即根据调用方法时传入的参数，动态判断是返回Mock数据还是真实数据。
* 支持返回多种Mock数据，包括JSON列表、JSON、null、String和基本类型，并支持抛出异常。
* 新增、编辑、删除、启用和禁用配置时，不需要重启目标应用程序。
* 自带Mock控制台，可通过界面管理Mock配置和查看目标应用程序。
* 自带示例应用程序，可作为目标应用程序以便用户学习。

## 1.2 组成部分

* Mock模块：即本工程中的mock-module。Mock模块的底层基于JVM Sandbox，其属于JVM Sandbox的一个用户模块，可被JVM Sandbox加载。由于JVM
  Sandbox只支持Linux、Unix和macOS操作系统，因此Mock模块也只能在这些操作系统上安装。
* Mock控制台后端：即本工程中的mock-be。它是Mock控制台的后端服务，使用MySQL作为数据库。
* Mock控制台前端：即[mock-fe](https://github.com/lujiatao2/mock-fe)工程。它是Mock控制台的前端服务。
* 示例应用程序：即本工程中的mock-example-app。示例应用程序提供多个示例接口，以便用户学习使用。

# 2 安装

&emsp;&emsp;由于安装过程会用到一些基础工具（比如wget），若提示“未找到命令”或“command not
found”，则需要先安装对应的基础工具。  
&emsp;&emsp;演示安装过程的服务器IP地址为192.168.3.102，用户需根据实际情况进行修改。

## 2.1 安装Mock模块

&emsp;&emsp;有两种方法可以安装Mock模块，任选其一。

### 2.1.1 在线安装

&emsp;&emsp;下载在线安装脚本：

```shell
wget https://github.com/lujiatao2/mock/releases/download/1.0.0/install-online.sh
```

&emsp;&emsp;给安装脚本增加执行权限：

```shell
chmod +x install-online.sh
```

&emsp;&emsp;执行安装脚本：

```shell
./install-online.sh
```

### 2.1.2 本地安装

&emsp;&emsp;下载源代码到本地：

```shell
git clone https://github.com/lujiatao2/mock.git
```

&emsp;&emsp;进入安装脚本所在目录：

```shell
cd mock/bin/
```

&emsp;&emsp;给安装脚本增加执行权限：

```shell
chmod +x install-local.sh
```

&emsp;&emsp;执行安装脚本：

```shell
./install-local.sh
```

## 2.2 安装Mock控制台

&emsp;&emsp;由于Mock控制台使用的是常用技术栈，因此安装方式很多，这里仅以Docker来演示安装过程，容器管理使用Docker
Compose（当然也可以使用Docker Swarm或Kubernetes等）。  
&emsp;&emsp;新增docker-compose.yml文件，文件内容如下：

```yaml
version: "3.3"
services:
  mock-mysql:
    image: mysql:5.7.33
    container_name: mock-mysql
    restart: always
    ports:
      - "10000:3306"
    environment:
      - MYSQL_ROOT_HOST=%
      - MYSQL_ROOT_PASSWORD=root123456
      - MYSQL_DATABASE=mock
      - TZ=Asia/Shanghai
    command: --character-set-server=utf8
    volumes:
      - /opt/mysql/mock:/var/lib/mysql
  mock-be:
    image: registry.cn-chengdu.aliyuncs.com/lujiatao/mock-be:1.0.0
    container_name: mock-be
    restart: always
    ports:
      - "10001:8080"
    environment:
      - MYSQL_URL=jdbc:mysql://mock-mysql:3306/mock
    depends_on:
      - mock-mysql
  mock-fe:
    image: registry.cn-chengdu.aliyuncs.com/lujiatao/mock-fe:1.0.0
    container_name: mock-fe
    restart: always
    ports:
      - "10002:80"
    environment:
      - MOCK_BE_IP=192.168.3.102
      - MOCK_BE_PORT=10001
      - MOCK_FE_PORT=10002
    depends_on:
      - mock-be
```

&emsp;&emsp;可能需要修改的配置：

* MySQL、Mock控制台后端和Mock控制台前端映射的宿主机端口分别为10000、10001和10002，需根据实际情况进行修改。
* MySQL root密码默认为root123456，需根据实际情况进行修改。
* Mock控制台后端IP地址默认为192.168.3.102，需根据实际情况进行修改。

&emsp;&emsp;执行以下命令启动Mock控制台：

```shell
docker-compose up -d
```

&emsp;&emsp;执行以下命令查看Mock控制台运行状态：

```shell
docker-compose ps
```

&emsp;&emsp;如果启动成功，那么mock-be、mock-fe和mock-mysql服务都处于Up状态，如下所示：

```shell
   Name                 Command               State                 Ports               
----------------------------------------------------------------------------------------
mock-be      java -jar mock-be.jar            Up      0.0.0.0:10001->8080/tcp           
mock-fe      /docker-entrypoint.sh /bin ...   Up      0.0.0.0:10002->80/tcp             
mock-mysql   docker-entrypoint.sh --cha ...   Up      0.0.0.0:10000->3306/tcp, 33060/tcp
```

&emsp;&emsp;访问 http://192.168.3.102:10002/ 显示Mock控制台首页，如下图所示：

![Mock控制台首页](https://raw.githubusercontent.com/lujiatao2/mock/master/src/main/resources/img/img-01.png)

# 3 配置

&emsp;&emsp;在对目标应用程序进行Mock之前，需求做一些配置工作。

## 3.1 安装示例应用程序

&emsp;&emsp;为了方便用户学习，这里提供了一个示例应用程序作为待Mock的目标应用程序。  
&emsp;&emsp;下载示例应用程序：

```shell
wget https://github.com/lujiatao2/mock/releases/download/1.0.0/mock-example-app-1.0.0.jar
```

&emsp;&emsp;执行以下命令启动示例应用程序：

```shell
java -jar mock-example-app-1.0.0.jar
```

&emsp;&emsp;由于示例应用程序默认使用8080端口，如果与现有端口冲突，可指定不同的端口号来启动，比如指定8090端口启动：

```shell
java -jar mock-example-app-1.0.0.jar --server.port=8090
```

&emsp;&emsp;启动成功后就可以调用示例应用程序的接口了：

```shell
curl http://192.168.3.102:8090/mock-example-app/by-id?id=1
```

&emsp;&emsp;接口返回如下：

```json
{
  "id": 1,
  "stringParameter": "String Parameter 0",
  "intParameter": 0,
  "integerParameter": 10,
  "doubleParameter": 0.0,
  "booleanParameter": true,
  "adoubleParameter": 1.0,
  "abooleanParameter": false
}
```

&emsp;&emsp;示例应用程序接口详见[5.1 示例应用程序接口](https://github.com/lujiatao2/mock#51-%E7%A4%BA%E4%BE%8B%E5%BA%94%E7%94%A8%E7%A8%8B%E5%BA%8F%E6%8E%A5%E5%8F%A3)。

## 3.2 配置Mock模块

&emsp;&emsp;Mock模块的配置文件是mock.properties，位于~/.sandbox-module/cfg目录，配置项说明如下：

* app.env：Mock应用环境，比如development、test、demo、production等，需根据实际情况进行修改，缺省值是unknown。
* app.name：Mock应用名称，比如order-server、pay-server等，需根据实际情况进行修改，缺省值是unknown。
* mock.be.url：Mock控制台后端URL地址，需根据实际情况进行修改，缺省值是 http://127.0.0.1:80 。

&emsp;&emsp;以下是演示使用的配置：

```properties
# Mock应用环境
app.env=demo
# Mock应用名称
app.name=mock-example-app
# Mock后端URL
mock.be.url=http://192.168.3.102:10001
```

## 3.3 接入Mock应用

&emsp;&emsp;接入Mock应用是指使用JVM Sandbox加载Mock模块生成Mock模块的实例，并将Mock模块实例注册到Mock控制台。  
&emsp;&emsp;进入JVM Sandbox脚本所在目录：

```shell
cd ~/sandbox/bin/
```

&emsp;&emsp;执行以下命令接入Mock应用：

```shell
./sandbox.sh -p `ps -ef | grep mock-example-app-1.0.0.jar | grep -v grep | awk '{print $2}'`
```

&emsp;&emsp;-p命令指定的是目标应用程序的进程号，这里使用了ps命令，并结合grep和awk命令查询进程号。  
&emsp;&emsp;访问 http://192.168.3.102:10002/mock-app ，可以看到目标应用程序已经接入了Mock控制台：

![Mock应用](https://raw.githubusercontent.com/lujiatao2/mock/master/src/main/resources/img/img-02.png)

&emsp;&emsp;点击查看日志可以看到当前Mock模块实例的日志：

![Mock模块实例日志](https://raw.githubusercontent.com/lujiatao2/mock/master/src/main/resources/img/img-03.png)

# 4 使用

## 4.1 静态Mock

&emsp;&emsp;首先新增一个Mock配置：

![新增Mock配置](https://raw.githubusercontent.com/lujiatao2/mock/master/src/main/resources/img/img-04.png)

&emsp;&emsp;以上配置将/mock-example-app/by-id接口的所有请求都返回以下硬编码的数据：

```json
{
  "id": 999,
  "stringParameter": "String Parameter 0",
  "intParameter": 0,
  "integerParameter": 10,
  "doubleParameter": 0.0,
  "booleanParameter": true,
  "adoubleParameter": 1.0,
  "abooleanParameter": false
}
```

&emsp;&emsp;执行以下命令调用/mock-example-app/by-id接口：

```shell
curl http://192.168.3.102:8090/mock-example-app/by-id?id=1
```

&emsp;&emsp;此时返回值的ID等于1，并不是999（即并不是配置的Mock数据）。Mock配置没有生效的原因是Mock模块实例没有重新加载配置，解决方法是手动点击刷新配置即可：

![刷新配置](https://raw.githubusercontent.com/lujiatao2/mock/master/src/main/resources/img/img-05.png)

&emsp;&emsp;除了新增Mock配置，编辑、删除、启用和禁用Mock配置时，都需要手动刷新配置才能生效。  
&emsp;&emsp;重新执行以下命令：

```shell
curl http://192.168.3.102:8090/mock-example-app/by-id?id=1
```

&emsp;&emsp;此时返回值的ID等于999，即返回值是配置的Mock数据。  
&emsp;&emsp;在调用/mock-example-app/by-id接口时，即使把id换成2、3、4、5、6，返回值仍然是配置的Mock数据，因此这种Mock配置是静态的。

## 4.2 动态Mock

&emsp;&emsp;如果根据调用方法时传入的参数，动态判断是返回Mock数据还是真实数据，这就是动态Mock。

### 4.2.1 全参数匹配

&emsp;&emsp;当id大于等于5时返回Mock数据，小于5时返回真实数据，可以这样来配置：

![动态Mock](https://raw.githubusercontent.com/lujiatao2/mock/master/src/main/resources/img/img-06.png)

&emsp;&emsp;手动刷新配置后，执行以下命令：

```shell
curl http://192.168.3.102:8090/mock-example-app/by-id?id=5
```

&emsp;&emsp;返回值的ID等于999：

```json
{
  "id": 999,
  "stringParameter": "String Parameter 0",
  "intParameter": 0,
  "integerParameter": 10,
  "doubleParameter": 0.0,
  "booleanParameter": true,
  "adoubleParameter": 1.0,
  "abooleanParameter": false
}
```

&emsp;&emsp;执行以下命令：

```shell
curl http://192.168.3.102:8090/mock-example-app/by-id?id=4
```

&emsp;&emsp;返回值的ID等于4：

```json
{
  "id": 4,
  "stringParameter": "String Parameter 3",
  "intParameter": 3,
  "integerParameter": 13,
  "doubleParameter": 0.3,
  "booleanParameter": false,
  "adoubleParameter": 1.3,
  "abooleanParameter": true
}
```

### 4.2.2 部分参数匹配

&emsp;&emsp;如果一个方法有多个参数，比如/mock-example-app/by-conditions接口对应的getByConditions方法就有多个参数，接口调用方式如下：

```shell
curl "http://192.168.3.102:8090/mock-example-app/by-conditions?stringParameter=String+Parameter+0&&intParameter=0&&integerParameter=10&&doubleParameter=0.0&&aDoubleParameter=1.0&&booleanParameter=true&&aBooleanParameter=false"
```

&emsp;&emsp;真实返回值如下：

```json
[
  {
    "id": 1,
    "stringParameter": "String Parameter 0",
    "intParameter": 0,
    "integerParameter": 10,
    "doubleParameter": 0.0,
    "booleanParameter": true,
    "adoubleParameter": 1.0,
    "abooleanParameter": false
  }
]
```

&emsp;&emsp;如果只想匹配intParameter等于0，那么可以将其他参数配成忽略：

![部分参数匹配](https://raw.githubusercontent.com/lujiatao2/mock/master/src/main/resources/img/img-07.png)

## 4.3 特殊的Mock

### 4.3.1 返回null

&emsp;&emsp;Mock数据填null：

![返回null](https://raw.githubusercontent.com/lujiatao2/mock/master/src/main/resources/img/img-08.png)

### 4.3.2 抛出异常

&emsp;&emsp;Mock数据直接填异常的全名：

![抛出异常](https://raw.githubusercontent.com/lujiatao2/mock/master/src/main/resources/img/img-09.png)

# 5 附录

## 5.1 示例应用程序接口

### 5.1.1 /mock-example-app/all

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getAll()，接口返回值如下：

```json
[
  {
    "id": 1,
    "stringParameter": "String Parameter 0",
    "intParameter": 0,
    "integerParameter": 10,
    "doubleParameter": 0.0,
    "booleanParameter": true,
    "adoubleParameter": 1.0,
    "abooleanParameter": false
  },
  {
    "id": 2,
    "stringParameter": "String Parameter 1",
    "intParameter": 1,
    "integerParameter": 11,
    "doubleParameter": 0.1,
    "booleanParameter": false,
    "adoubleParameter": 1.1,
    "abooleanParameter": true
  },
  {
    "id": 3,
    "stringParameter": "String Parameter 2",
    "intParameter": 2,
    "integerParameter": 12,
    "doubleParameter": 0.2,
    "booleanParameter": true,
    "adoubleParameter": 1.2,
    "abooleanParameter": false
  },
  {
    "id": 4,
    "stringParameter": "String Parameter 3",
    "intParameter": 3,
    "integerParameter": 13,
    "doubleParameter": 0.3,
    "booleanParameter": false,
    "adoubleParameter": 1.3,
    "abooleanParameter": true
  },
  {
    "id": 5,
    "stringParameter": "String Parameter 4",
    "intParameter": 4,
    "integerParameter": 14,
    "doubleParameter": 0.4,
    "booleanParameter": true,
    "adoubleParameter": 1.4,
    "abooleanParameter": false
  },
  {
    "id": 6,
    "stringParameter": "String Parameter 5",
    "intParameter": 5,
    "integerParameter": 15,
    "doubleParameter": 0.5,
    "booleanParameter": false,
    "adoubleParameter": 1.5,
    "abooleanParameter": true
  },
  {
    "id": 7,
    "stringParameter": "String Parameter 6",
    "intParameter": 6,
    "integerParameter": 16,
    "doubleParameter": 0.6,
    "booleanParameter": true,
    "adoubleParameter": 1.6,
    "abooleanParameter": false
  },
  {
    "id": 8,
    "stringParameter": "String Parameter 7",
    "intParameter": 7,
    "integerParameter": 17,
    "doubleParameter": 0.7,
    "booleanParameter": false,
    "adoubleParameter": 1.7,
    "abooleanParameter": true
  },
  {
    "id": 9,
    "stringParameter": "String Parameter 8",
    "intParameter": 8,
    "integerParameter": 18,
    "doubleParameter": 0.8,
    "booleanParameter": true,
    "adoubleParameter": 1.8,
    "abooleanParameter": false
  },
  {
    "id": 10,
    "stringParameter": "String Parameter 9",
    "intParameter": 9,
    "integerParameter": 19,
    "doubleParameter": 0.9,
    "booleanParameter": false,
    "adoubleParameter": 1.9,
    "abooleanParameter": true
  }
]
```

### 5.1.2 /mock-example-app/by-conditions?stringParameter=String+Parameter+0&&intParameter=0&&integerParameter=10&&doubleParameter=0.0&&aDoubleParameter=1.0&&booleanParameter=true&&aBooleanParameter=false

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getByConditions(String stringParameter,
int intParameter, Integer integerParameter, double doubleParameter, Double aDoubleParameter, boolean booleanParameter,
Boolean aBooleanParameter)，接口返回值如下：

```json
[
  {
    "id": 1,
    "stringParameter": "String Parameter 0",
    "intParameter": 0,
    "integerParameter": 10,
    "doubleParameter": 0.0,
    "booleanParameter": true,
    "adoubleParameter": 1.0,
    "abooleanParameter": false
  }
]
```

### 5.1.3 /mock-example-app/by-id?id=1

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getById(int id)，接口返回值如下：

```json
{
  "id": 1,
  "stringParameter": "String Parameter 0",
  "intParameter": 0,
  "integerParameter": 10,
  "doubleParameter": 0.0,
  "booleanParameter": true,
  "adoubleParameter": 1.0,
  "abooleanParameter": false
}
```

### 5.1.4 /mock-example-app/string

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getString()，接口返回值如下：

```text
我是String
```

### 5.1.5 /mock-example-app/byte

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getByte()，接口返回值如下：

```text
1
```

### 5.1.6 /mock-example-app/a-byte

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getAByte()，接口返回值如下：

```text
2
```

### 5.1.7 /mock-example-app/short

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getShort()，接口返回值如下：

```text
3
```

### 5.1.8 /mock-example-app/a-short

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getAShort()，接口返回值如下：

```text
4
```

### 5.1.9 /mock-example-app/int

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getInt()，接口返回值如下：

```text
5
```

### 5.1.10 /mock-example-app/integer

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getInteger()，接口返回值如下：

```text
6
```

### 5.1.11 /mock-example-app/long

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getLong()，接口返回值如下：

```text
7
```

### 5.1.12 /mock-example-app/a-long

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getALong()，接口返回值如下：

```text
8
```

### 5.1.13 /mock-example-app/float

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getFloat()，接口返回值如下：

```text
9.0
```

### 5.1.14 /mock-example-app/a-float

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getAFloat()，接口返回值如下：

```text
10.0
```

### 5.1.15 /mock-example-app/double

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getDouble()，接口返回值如下：

```text
11.0
```

### 5.1.16 /mock-example-app/a-double

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getADouble()，接口返回值如下：

```text
12.0
```

### 5.1.17 /mock-example-app/boolean

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getBoolean()，接口返回值如下：

```text
true
```

### 5.1.18 /mock-example-app/a-boolean

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getABoolean()，接口返回值如下：

```text
false
```

### 5.1.19 /mock-example-app/char

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getChar()，接口返回值如下：

```text
"a"
```

### 5.1.20 /mock-example-app/character

&emsp;&emsp;对应的方法为com.lujiatao.mock.example.app.MockExampleAppController#getCharacter()，接口返回值如下：

```text
"b"
```

## 5.2 开发者注意事项

&emsp;&emsp;针对想对本项目做贡献的开发者，有几点需要注意：

* Mock模块使用纯Java开发，确保JDK版本不低于JDK 8。
* Mock控制台后端使用Spring Boot + MyBatis + MySQL开发，确保JDK版本不低于JDK 8，以及MySQL数据库已安装。服务默认端口为8080，需根据实际情况进行修改。
* [Mock控制台前端](https://github.com/lujiatao2/mock-fe)使用Node.js + Vue.js + Element Plus开发，启动前请确保Mock控制台后端服务已启动。
* 示例应用程序使用Spring Boot开发，确保JDK版本不低于JDK 8。服务默认端口为8080，需根据实际情况进行修改。