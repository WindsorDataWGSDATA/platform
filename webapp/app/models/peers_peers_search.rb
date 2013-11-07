# == Schema Information
#
# Table name: searches
#
#  id          :integer          not null, primary key
#  user_id     :integer
#  json_query  :text
#  created_at  :datetime         not null
#  updated_at  :datetime         not null
#  company_id  :integer
#  report_type :string(255)
#  peers       :string(255)
#  tickers     :string(255)
#  type        :string(255)
#

class PeersPeersSearch < Search
  attr_accessible :peers, :tickers

  def search_type
    Constants::PEERS_PEERS_SEARCH
  end
end
