echo "define host{
        host_name                       shorthostname 
        aliasa                           shorthostname 
        address                         privateip
        check_command                  	check-host-alive
        use                             host_check,host-pnp
	}" >> env_vars.txt
