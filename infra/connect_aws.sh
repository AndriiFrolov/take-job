chmod 400 work_mac_os.cer
echo "Creating SSH connectionto aws instance"
ssh -i work_mac_os.cer ec2-user@ec2-18-208-132-161.compute-1.amazonaws.com

ssh -i work_mac_os.cer ec2-user@ec2-18-212-18-127.compute-1.amazonaws.com

