class SearchController < ApplicationController
  before_filter :get_form_values, only: [:full_search, :recent_search]

  def quick_search
    authorize!(:perform, :quick_search)
    @groups = current_user.is_client? ? Group.by_company(current_user.company) : @groups = Group.all

    # Get n recent searches
    n = 5
    if current_user.is_super?
      @searches = Search.last_ordered_by_date(n)
    elsif current_user.is_client?
      @searches = Search.by_company(current_user.company, n)
    end

  end
  
  def full_search
    authorize!(:perform, :full_search)
  end

  def results
    params_hash = params.except(:controller, :action, :authenticity_token, :utf8, :role_form)
    Search.create(user: current_user, json_query: params_hash.to_json, company: current_user.company)
    json_query = QueryGenerator.json_query(params_hash)
    path = Rails.application.config.backend_host + Rails.application.config.post_query_path

    @tickers = []
    RestClient.post(path, json_query, {content_type: :json}) do |response, request|
      @tickers = JSON.parse(response)
    end
  end

  def download
    json_query = params.except(:controller, :action, :authenticity_token, :utf8, :role_form).to_json.gsub(/(")(\d+)(")/, ' \2')
    perform_search(json_query, Constants::TOP5_REPORT)
  end

  def group_search
    tickers = Group.find(params[:group]).tickers.map(&:cusip)
    json_query = { range: 1, companies: tickers }.to_json.gsub(/(")(\d+)(")/, ' \2')
    perform_search(json_query, params[:report])
    end
  end

  def recent_search
    authorize!(:perform, :recent_search)
    @params_hash = JSON.parse(Search.find(params[:id]).json_query)
  end

  private
  def get_form_values
    @primary_roles = PrimaryRole.order("name asc")
    @eq_comp_values = EquityCompensation.order("value asc")
    @cash_comp_values = CashCompensation.order("value asc")    
  end

  def perform_search(json_query, report_type)
    path = Rails.application.config.backend_host + Rails.application.config.post_download_report_path  
    RestClient.post(path, json_query, {content_type: :json}) do |response, request|
      if response.code == 200
        send_data(response.body, filename: "report.xls")
      else
        flash[:error] = "There was an error"
        render "results"
      end
    end
  end

end