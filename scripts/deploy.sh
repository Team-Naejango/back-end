#! /bin/bash

tag=$1
strategy=$2
log_path=/home/ec2-user/log
path=/home/ec2-user/cicd

if [ "$strategy" == "blue-green" ]; then
	sudo sh $path/blue-green-deploy.sh $tag $log_path
elif [ "$strategy" == "rolling" ]; then
	sudo sh $path/rolling-deploy.sh $tag $log_path
elif [ "$strategy" == "develop" ]; then
	sudo sh $path/develop-deploy.sh $tag $log_path
fi
