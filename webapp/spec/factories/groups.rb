FactoryGirl.define do
  factory :group do
    sequence(:name) {|n| "Group #{n}" }
    company
  end
end