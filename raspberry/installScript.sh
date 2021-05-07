#!/bin/bash

if [ "$EUID" -ne 0 ]; then
        echo "Please run as root"
        exit 1
fi

WORKDIR=$PWD

apt update
apt upgrade

apt install git -y
apt install cmake -y

apt install openjdk-8-jdk -y
update-alternatives --config java

echo "\nexport JAVA_HOME=/usr/lib/jvm/java-8-openjdk-armhf/" >> ~/.bashrc
source ~/.bashrc

apt install maven -y
apt install libglib2.0-dev libdbus-1-dev libudev-dev libical-dev libreadline-dev -y

mkdir ~/bluez
cd ~/bluez
wget http://www.kernel.org/pub/linux/bluetooth/bluez-5.47.tar.xz
tar -xf bluez-5.47.tar.xz && cd bluez-5.47
./configure --prefix=/usr --mandir=/usr/share/man --sysconfdir=/etc --localstatedir=/var
make
make install

cd $WORKDIR

bluetoothConfig=$(cat /etc/dbus-1/system.d/bluetooth.conf)
insertText=( "<policy group=\"bluetooth\">\n"
        "<allow send_destination=\"org.bluez\"/>\n"
"</policy>" )
if [[ $bluetoothConfig != *"${insertText[*]}"* ]];
then
        sed -i '/<\/busconfig>/i'\ "${insertText[*]}\ " /etc/dbus-1/system.d/bluetooth.conf
fi

adduser --system --no-create-home --group --disabled-login openhab
usermod -a -G bluetooth openhab
systemctl daemon-reload
systemctl restart bluetooth

apt install graphviz -y
apt install doxygen -y

git clone https://github.com/intel-iot-devkit/tinyb.git && cd tinyb
mkdir build
cd build
sudo -E cmake -DBUILDJAVA=ON -DCMAKE_INSTALL_PREFIX=/usr ..
make
make install

cd $WORKDIR
