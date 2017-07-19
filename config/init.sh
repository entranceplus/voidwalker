echo 'Installing java'
apt-get update -y

apt-get install default-jdk -y
java -version

# Install Mysql server
echo 'Mysql install..'
apt-get update -y
rm *.run
wget https://www.apachefriends.org/xampp-files/5.6.30/xampp-linux-x64-5.6.30-0-installer.run
chmod a+x xampp-linux-x64-5.6.30-0-installer.run
./xampp-linux-x64-5.6.30-0-installer.run --mode unattended
/opt/lampp/lampp startmysql
/opt/lampp/bin/mysql -u root -e "create database if not exists voidwalker"

echo 'Copying config'
cp ./voidwalker/voidwalker.service /lib/systemd/system/voidwalker.service

echo 'Starting service'
export DATABASE_URL="jdbc:mysql://localhost/voidwalker?user=root&password="
# java -jar /root/voidwalker/voidwalker.jar migrate
systemctl daemon-reload
systemctl enable voidwalker
systemctl restart voidwalker

echo 'Setting up nginx'
/opt/lampp/lampp stopapache
apt-get install nginx -y
cp -ru /root/voidwalker/default /etc/nginx/sites-available/
nginx -t
systemctl restart nginx
