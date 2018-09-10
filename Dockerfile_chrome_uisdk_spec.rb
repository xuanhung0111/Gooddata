
require "serverspec"

describe "Dockerfile" do
  before(:all) do

    set :backend, :docker
    set :docker_image, ENV['IMAGE_ID']
  end

  packages = ['nodejs', 'jq']

  it "installs required packages" do
    packages.each do |p|
      expect(package(p)).to be_installed
    end
  end

  it "installs nodejs" do
    expect(command("node --version").stdout).to contain ("v8.10.0")
  end

  it "installs yarn" do
    expect(command("yarn --version").stdout).to contain /[0-9]*/
  end

  it "installs create-react-app" do
    expect(command("create-react-app -Version").stdout).to contain /[0-9]*/
  end

  it "installs gdc-catalog-export" do
    expect(command("gdc-catalog-export -Version").stdout).to contain /[0-9]*/
  end

end
