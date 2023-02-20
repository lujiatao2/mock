#!/bin/sh

# 该脚本用于在线安装Mock模块。

echo "====================          开始安装...          ===================="

exit_with_err_msg() {
  if [ -n "${1}" ]; then
    # 把标准输出重定向到标准错误输出
    echo "${1}" 1>&2
  fi
  exit 1
}

# JVM Sandbox用户模块目录
MODULE_HOME=${HOME}/.sandbox-module

echo "====================          Step 1：创建目录          ===================="
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

echo "====================          Step 2：开始安装Mock模块...          ===================="
wget https://github.com/lujiatao2/mock/releases/download/1.0.0/mock-1.0.0-bin.zip || exit_on_err "安装Mock模块失败。"
unzip mock-1.0.0-bin.zip -d "${MODULE_HOME}" || exit_on_err "安装Mock模块失败。"
echo "====================          安装Mock模块成功。          ===================="

echo "====================          安装成功。          ===================="
