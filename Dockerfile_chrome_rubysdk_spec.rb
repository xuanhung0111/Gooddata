
require "serverspec"

describe "Dockerfile" do
  before(:all) do

    set :backend, :docker
    set :docker_image, ENV['IMAGE_ID']
  end

  it "installs gooddata" do
    expect(command("gooddata --version").stdout).to contain ("2.1.3")
  end

end
