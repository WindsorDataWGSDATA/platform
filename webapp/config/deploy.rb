# config valid only for Capistrano 3.1
lock '3.1.0'

set :application, 'windsor'
set :repo_url, 'git@github.com:WindsorData/platform.git'

set :stages, ["staging", "production"]
set :default_stage, "staging"

set :deploy_to, "/home/ubuntu/windsor"
set :deploy_via, :remote_cache
set :use_sudo, false

# Set up a strategy to deploy only a project directory (not the whole repo)
set :git_strategy, RemoteCacheWithProjectRootStrategy
set :project_root, 'webapp'

set :scm, "git"

set :ssh_options, {
  forward_agent: true,
  keys: "#{Dir.home}/.ssh/windsor.pem"
}

set :rbenv_custom_path, "/opt/rbenv"
set :rbenv_ruby, File.read('.ruby-version').strip

set :pty, true
# set :default_env, { 
#   path: "/opt/rbenv/shims/ruby:$PATH"
# }

set :default_env, {
  'PATH' => "/opt/rbenv/shims/ruby:$PATH"
  # 'RAILS_RELATIVE_URL_ROOT' => "/webapp"
}

set :unicorn_bin, "unicorn_rails"

# set :unicorn_rails_env, ->{ fetch :rails_env, "production" }

# set :bundle_gemfile, -> { release_path.join('/webapp/Gemfile') }

# Default branch is :master
# ask :branch, proc { `git rev-parse --abbrev-ref HEAD`.chomp }

# Default deploy_to directory is /var/www/my_app
# set :deploy_to, '/var/www/my_app'

# Default value for :scm is :git
# set :scm, :git

# Default value for :format is :pretty
# set :format, :pretty

# Default value for :log_level is :debug
# set :log_level, :debug

# Default value for :pty is false

# Default value for :linked_files is []
# set :linked_files, %w{config/database.yml}

# Default value for linked_dirs is []
# set :linked_dirs, %w{bin log tmp/pids tmp/cache tmp/sockets vendor/bundle public/system}

# Default value for default_env is {}

# Default value for keep_releases is 5
set :keep_releases, 5

namespace :deploy do

  desc "Upload mailer credentials"
  task :upload_mailer_config do
    on roles(%w{web app db}), in: :sequence, wait: 5 do
      upload! "config/mailer.yml", "#{release_path}/config/mailer.yml"
    end
  end

  task :start_unicorn do
    invoke 'unicorn:restart'
  end

  task :stop_unicorn do
    invoke 'unicorn:stop'
  end

  task :create_pids_directory do
    on roles(%w{web app db}), in: :sequence, wait: 5 do
      execute "mkdir -p #{current_path}/tmp/pids"
    end
  end

  after :updating, :upload_mailer_config

  before :publishing, :create_pids_directory
  before :publishing, :stop_unicorn
  after :publishing, :cleanup

  after :published, :start_unicorn

end
