# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  # All Vagrant configuration is done here. The most common configuration
  # options are documented and commented below. For a complete reference,
  # please see the online documentation at vagrantup.com.

  # Every Vagrant virtual environment requires a box to build off of.
  config.berkshelf.enabled = true

  config.omnibus.chef_version =  "10.14.2"

  config.vm.box = "windsor"

  config.vm.provider :aws do |aws, override|
    aws.access_key_id = "ACCESS KEY ID"
    aws.secret_access_key = "SECRET ACCESS KEY"
    aws.keypair_name = "zauber"
    aws.region = "us-west-2"
    aws.instance_type = "m1.small"
    aws.ami = "ami-70f96e40"

    override.ssh.username = "ubuntu"
    override.ssh.private_key_path = "zauber.pem"
  end


  config.vm.provider "virtualbox" do |vbox|
    vbox.customize ["modifyvm", :id, "--memory", "1128"]
  end

  # The url from where the 'config.vm.box' box will be fetched if it
  # doesn't already exist on the user's system.
  # config.vm.box_url = "http://domain.com/path/to/above.box"

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # config.vm.network :forwarded_port, guest: 80, host: 8080

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network :private_network, ip: "192.168.33.10"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network :public_network

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  # config.vm.provider :virtualbox do |vb|
  #   # Don't boot with headless mode
  #   vb.gui = true
  #
  #   # Use VBoxManage to customize the VM. For example to change memory:
  #   vb.customize ["modifyvm", :id, "--memory", "1024"]
  # end
  #
  # View the documentation for the provider you're using for more
  # information on available options.

  # Enable provisioning with chef solo, specifying a cookbooks path, roles
  # path, and data_bags path (all relative to this Vagrantfile), and adding
  # some recipes and/or roles.
  #
  config.vm.provision :chef_solo do |chef|
     chef.add_recipe "application"
     chef.json = {
      postgresql: {
        password: {
          postgres: 'postgres'
        },
        pg_hba: [
          {:type => 'local', :db => 'all', :user => 'postgres', :addr => nil, :method => 'trust'},
          {:type => 'local', :db => 'all', :user => 'all', :addr => nil, :method => 'trust'},
          {:type => 'host', :db => 'all', :user => 'all', :addr => 'all', :method => 'trust'}
        ]
      }
    }
  end
end
