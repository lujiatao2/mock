#!/bin/sh

# 该脚本用于打包Mock模块。

echo "====================          开始打包...          ===================="

exit_with_err_msg() {
  if [ -n "${1}" ]; then
    # 把标准输出重定向到标准错误输出
    echo "${1}" 1>&2
  fi
  exit 1
}

# 成品目录
PRODUCT_DIR=mock

echo "====================          Step 1：创建目录          ===================="
mkdir -p ${PRODUCT_DIR} || exit_with_err_msg "没有权限，无法创建目录。"
mkdir -p ${PRODUCT_DIR}/cfg || exit_with_err_msg "没有权限，无法创建目录。"

echo "====================          Step 2：开始打包...          ===================="
mvn clean package -f ../pom.xml || exit_with_err_msg "打包失败。"
echo "====================          打包成功。          ===================="

echo "====================          Step 3：拷贝成品到成品目录。          ===================="
cp ./mock-logback.xml ${PRODUCT_DIR}/cfg/mock-logback.xml &&
  cp ./mock.properties ${PRODUCT_DIR}/cfg/mock.properties &&
  cp ../mock-module/target/mock-module-*-jar-with-dependencies.jar ${PRODUCT_DIR}/mock-module.jar

echo "====================          Step 4：压缩成品          ===================="
cd ${PRODUCT_DIR}/ || exit_with_err_msg "压缩成品失败。"
zip -r mock-1.0.0-bin.zip ./* || exit_with_err_msg "压缩成品失败。"
cd - || exit_with_err_msg "压缩成品失败。"
echo "====================          打包成功。          ===================="
