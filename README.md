# chatterade
An anonymous, peer-to-peer chat network, compatible with IRC clients.

## Install Java 8

sudo apt install openjdk-8-jdk-headless

## Install libsodium

tar -xvzf libsodium-1.0.17.tar.gz
cd libsodium-1.0.17
./configure
make && make check
sudo make install
sudo ldconfig

