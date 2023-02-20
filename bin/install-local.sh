#!/bin/sh

# 该脚本用于已经下载源代码，在本地安装Mock模块。

echo "====================          开始安装...          ===================="

exit_with_err_msg() {
  if [ -n "${1}" ]; then
    # 把标准输出重定向到标准错误输出
    echo "${1}" 1>&2
  fi
  exit 1
}

# 成品目录
PRODUCT_DIR=mock
# JVM Sandbox用户模块目录
MODULE_HOME=${HOME}/.sandbox-module

echo "====================          Step 1：创建目录          ===================="
mkdir -p ${PRODUCT_DIR} || exit_with_err_msg "没有权限，无法创建目录。"
mkdir -p ${PRODUCT_DIR}/cfg || exit_with_err_msg "没有权限，无法创建目录。"
if [ ! -d "${MODULE_HOME}" ]; then
  mkdir -p "${MODULE_HOME}" || exit_with_err_msg "没有权限，无法创建目录。"
fi

echo "====================          Step 2：开始安装JVM Sandbox...          ===================="
wget https://ompc.oss-cn-hangzhou.aliyuncs.com/jvm-sandbox/release/sandbox-1.3.3-bin.zip || exit_on_err "安装JVM Sandbox失败。"
unzip sandbox-1.3.3-bin.zip -d "${HOME}" || exit_on_err "安装JVM Sandbox失败。"
cd "${HOME}"/sandbox/ || exit_on_err "安装JVM Sandbox失败。"
./install-local.sh || exit_on_err "安装JVM Sandbox失败。"
cd - || exit_on_err "安装JVM Sandbox失败。"
echo "====================          安装JVM Sandbox成功。          ===================="

echo "====================          Step 3：开始打包...          ===================="
mvn clean package -f ../pom.xml || exit_with_err_msg "打包失败。"
echo "====================          打包成功。          ===================="

echo "====================          Step 4：拷贝成品到成品目录。          ===================="
cp ./mock-logback.xml ${PRODUCT_DIR}/cfg/mock-logback.xml &&
  cp ./mock.properties ${PRODUCT_DIR}/cfg/mock.properties &&
  cp ../mock-module/target/mock-module-*-jar-with-dependencies.jar ${PRODUCT_DIR}/mock-module.jar

echo "====================          Step 5：拷贝成品到安装目录          ===================="
cp -r ${PRODUCT_DIR}/* "${HOME}"/.sandbox-module || exit_with_err_msg "没有权限，无法拷贝文件。"

echo "====================          安装成功。          ===================="
