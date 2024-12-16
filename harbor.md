# 安装

```shell
## 离线安装

# 下载
wget https://github.com/goharbor/harbor/releases/download/v2.11.2/harbor-offline-installer-v2.11.2.tgz


#解压Harbor安装包
tar xvf harbor-offline-installer-v2.11.2.tgz

cd harbor

# 4. 配置Harbor（请根据需要修改harbor.yml文件）
cp harbor.yml.tmpl harbor.yml
# 编辑harbor.yml，例如修改hostname
 


```

```shell

vi /usr/local/harbor/harbor.yml
# 将hostname改成本机IP或域名，不要用localhost，127.0.0.1或0.0.0.0，冒号后面都有一个空格
hostname: harbor1.dev   #多个harbor服务要区分

# 将http端口改成10080，因为默认用的80端口已经被占用，http可以指定任意端口
http:
  port: 10080
# 配置https的端口，只能使用443端口，更改证书路径，证书路径为刚刚生成的https证书的实际路径
https:
  port: 10443
  certificate: /app/cert/harbor/harbor-server.crt
  private_key: /app/cert/harbor/harbor-server.key
# 修改后台管理密码
harbor_admin_password: harbor12345
# harbor的内部数据库密码
database:
  password: harbor123
# 修改harbor数据存储路径与日志存储路径,目录要先创建好并赋予777权限
data_volume: /data/harbor-data
# 修改日志存放路径，默认路径为/var/log/harbor
log:
  local:
    localtion: /data/harbor-log


```



```shell
# 5. 安装Harbor
./install.sh
修改harbor.yml之后需要执行
./prepare
docker-compose down
docker-compose up -d


// 1、打开daemon.json
vi /etc/docker/daemon.json

// 2、添加harbor访问信息，并保存
将"insecure-registries": ["主机ip:harbor端口"]添加到里面，例如：
{
  "registry-mirrors": ["https://95c1opgi.mirror.aliyuncs.com"],
  "insecure-registries": ["192.168.56.10:82"]
}
其中主机ip“192.168.56.10”是安装harbor的主机ip，而端口“82”是我在harbor.yml中设置的http.port值

// 3、应用docker配置，并重启docker
systemctl restart docker
systemctl restart docker


# harbor 自启动
// 1、编辑harbor.service文件
vi /usr/lib/systemd/system/harbor.service

// 2、将以下内容填充到上述文件中，并保存
[Unit]
Description=Harbor
After=docker.service systemd-networkd.service systemd-resolved.service
Requires=docker.service
Documentation=http://github.com/vmware/harbor
[Service]
Type=simple
Restart=on-failure
RestartSec=5
ExecStart=/usr/local/bin/docker-compose -f /opt/harbor/docker-compose.yml up
ExecStop=/usr/local/bin/docker-compose -f /opt/harbor/docker-compose.yml down
[Install]
WantedBy=multi-user.target

// 3、设置开机自启
    systemctl enable harbor


```

