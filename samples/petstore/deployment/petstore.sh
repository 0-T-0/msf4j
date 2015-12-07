#!/bin/bash

# exporting paths
source path.sh


# checking IS pack availability
if [ ! -f packs/$IS_PACK ];then
    echo "WSO2 Identity Server pack is not available at packs directry. Please copy Identity Server pack, update path.sh and re-run petstore.sh"
    exit
fi

cd $HOME/packs
[ -d ${IS_PACK%.zip} ] && rm -fr ${IS_PACK%.*}
unzip $IS_PACK
cd ${IS_PACK%.zip}/bin/
echo "--------------------------------------------------------------"
echo "Starting WSO2 Identity Server"
echo "--------------------------------------------------------------"
./wso2server.sh start

echo "--------------------------------------------------------------"
echo "Creating Kube-System Namespace, Kube-DNS, Kube-UI"
echo "--------------------------------------------------------------"
kubectl create -f $VAGRANT_HOME/plugins/namespace/kube-system.json
kubectl create -f $VAGRANT_HOME/plugins/dns/dns-service.yaml
kubectl create -f $VAGRANT_HOME/plugins/dns/dns-controller.yaml
kubectl create -f $VAGRANT_HOME/plugins/kube-ui/kube-ui-controller.yaml
kubectl create -f $VAGRANT_HOME/plugins/kube-ui/kube-ui-service.yaml
sleep 20


echo "--------------------------------------------------------------"
echo "Creating services for external endpoints"
echo "--------------------------------------------------------------"
cd $HOME
kubectl create -f external-endpoints/


echo "--------------------------------------------------------------"
echo "Deploying Redis Cluster"
echo "--------------------------------------------------------------"
cd $HOME
cd $REDIS_HOME/container/kubernetes/
kubectl create -f redis-master.yaml
sleep 30
kubectl create -f redis-sentinel-service.yaml
kubectl create -f redis-controller.yaml
kubectl create -f redis-sentinel-controller.yaml
kubectl scale rc redis --replicas=3
sleep 30
kubectl scale rc redis-sentinel --replicas=3
sleep 30
kubectl delete pods redis-master


sleep 20

echo "--------------------------------------------------------------"
echo "Deploying Pet"
echo "--------------------------------------------------------------"

cd $HOME
cd $PET_HOME/container/kubernetes/
kubectl create -f .


echo "--------------------------------------------------------------"
echo "Deploying FileServer"
echo "--------------------------------------------------------------"
cd $HOME
kubectl label nodes 172.17.8.102 disktype=ssd
cd $FILESERVER_HOME/container/kubernetes/
kubectl create -f .


echo "--------------------------------------------------------------"
echo "Deploying FrontEnd Admin"
echo "--------------------------------------------------------------"
cd $HOME
cd $FRONTEND_ADMIN/container/kubernetes/
kubectl create -f .

echo "--------------------------------------------------------------"
echo "Deploying FrontEnd User"
echo "--------------------------------------------------------------"
cd $HOME
cd $FRONTEND_USER/container/kubernetes/
kubectl create -f .


echo "--------------------------------------------------------------"
echo "Deploying Security"
echo "--------------------------------------------------------------"
cd $HOME
cd $SECURITY/container/kubernetes/
kubectl create -f .


echo "--------------------------------------------------------------"
echo "Deploying Transaction"
echo "--------------------------------------------------------------"
cd $HOME
cd $TRANSACTION/container/kubernetes/
kubectl create -f .

