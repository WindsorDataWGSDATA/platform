# == Schema Information
#
# Table name: users
#
#  id                     :integer          not null, primary key
#  email                  :string(255)      default(""), not null
#  encrypted_password     :string(255)      default(""), not null
#  reset_password_token   :string(255)
#  reset_password_sent_at :datetime
#  remember_created_at    :datetime
#  sign_in_count          :integer          default(0)
#  current_sign_in_at     :datetime
#  last_sign_in_at        :datetime
#  current_sign_in_ip     :string(255)
#  last_sign_in_ip        :string(255)
#  created_at             :datetime         not null
#  updated_at             :datetime         not null
#  role                   :string(255)
#  company_id             :integer
#

class User < ActiveRecord::Base
  ROLES = %w[super admin client]
  before_validation :generate_random_password
  
  devise :database_authenticatable,
         :recoverable, :rememberable, :trackable, :validatable

  attr_accessible :email, :password, :password_confirmation, :remember_me, :role, :company

  belongs_to :company

  # Dynamic role methods definition
  ROLES.each do |k|
    define_method "is_#{k}?" do
      role == k
    end
  end
  
  private
  def generate_random_password
    unless self.password && self.password_confirmation && self.password == self.password_confirmation
      self.password = self.password_confirmation = Devise.friendly_token.first(Rails.application.config.devise.password_length.min)  
    end
  end
end