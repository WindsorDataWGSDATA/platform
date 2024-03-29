class Ability
  include CanCan::Ability

  def initialize(user)
    user ||= User.new
    case user.role
    when 'super'
      can   :manage, User
      can   :manage, Company
      can   :audit, :upload_log
      can   [:read, :create], Group
      can   :upload, :file
      can   :read_multiple, Array do |arr|
        arr.inject(true){|r, el| r && can?(:read, el)}
      end
      can   :perform, :quick_search
      can   :perform, :full_search
      can   :perform, :recent_search
      can   :perfom,  :delete_info
      can   :perfom,  :delete_db
    when 'admin'
      can   :upload, :file
      can   :perform, :full_search
      can [:update_account, :edit_account], User do |u|
        u == user
      end
      can   :manage, Company
      can   :audit, :upload_log
      can   [:read, :create], Group
      can   :upload, :file
      can   :read_multiple, Array do |arr|
        arr.inject(true){|r, el| r && can?(:read, el)}
      end
      can   :perform, :quick_search
      can   :perform, :full_search
      can   :perfom,  :delete_info
      # can   :perform, :recent_search
    when 'client'
      can   :create, Group
      can   :destroy, Group do |g|
        g.user == user
      end
      can [:update_account, :edit_account], User do |u|
        u == user
      end
      can   :read, Group do |g|
        g.company == user.company && user.company
      end
      can   :read_multiple, Array do |arr|
        arr.inject(true){|r, el| r && can?(:read, el)}
      end
      can   :perform, :quick_search
      can   :perform, :full_search
      # can   :perform, :recent_search
    when 'client_peer_peer'
      can [:update_account, :edit_account], User do |u|
        u == user
      end
    end
  end
end
