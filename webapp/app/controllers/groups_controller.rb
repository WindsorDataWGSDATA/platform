class GroupsController < ApplicationController
  inherit_resources

  def create
    authorize!(:create, Group)
    @group = Group.new(params[:group])
    @group.company = current_user.company if current_user.is_client?
    create! { groups_path }
  end
  
  def index
    @group = Group.new
    @groups = current_user.is_client? ? Group.by_company(current_user.company).order("name asc").to_a : Group.order("name asc").to_a
    authorize!(:read_multiple, @groups)
  end
  def update
    update! { groups_path }
  end

  # GET /groups/tickers.json
  def tickers
    authorize!(:create, Group)
    @tickers = Ticker.containing_chars(params[:q])
    respond_to do |format|
      format.html
      format.json { render json: @tickers.map(&:attributes) }
    end
  end
end