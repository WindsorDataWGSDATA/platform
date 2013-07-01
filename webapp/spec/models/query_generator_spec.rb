require 'spec_helper'

describe QueryGenerator do
  subject { QueryGenerator.json_query(params_hash) }
  
  context "When there is one form with one key-value field" do
    let(:params_hash){
      Hash[
        "role_form_1"=> {
          "pay_rank"=>"CTO"
        },
        'adv_1' => '4'
      ] 
    }
    it { should ==  "{'executives':[{'executivesFilters':[{'key':'pay_rank','value':'CTO'}]}],'advanced':[{'key':'adv_1','values': 4}]}" }
  end

  context "When there are multiple forms with only key-value fields" do
    let(:params_hash){
      Hash[
        "role_form_1"=> {
          "pay_rank"=>"CTO"
        },
        "role_form_2"=> {
          "pay_rank"=>"CFO"
        }
      ] 
    }
    it { should ==  "{'executives':[{'executivesFilters':[{'key':'pay_rank','value':'CTO'}]},{'executivesFilters':[{'key':'pay_rank','value':'CFO'}]}]}" }
  end

  context "When there is one form with key-value and range fields" do
    let(:params_hash){
      Hash[
        "role_form_1"=> {
          "pay_rank"=>"CTO",
          "ceo_tenure"=>{"gt"=>"1", "lt"=>"3"},
        },
        "role_form_2"=> {
          "role"=>"1"
        }
      ] 
    }
    it { should ==  "{'executives':[{'executivesFilters':[{'key':'pay_rank','value':'CTO'},{'key':'ceo_tenure','operators':[{'gt': 1,'lt': 3}]}]},{'executivesFilters':[{'key':'role','value': 1}]}]}" }
  end
end