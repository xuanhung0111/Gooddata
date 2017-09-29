
require "serverspec"

describe "Dockerfile" do
  before(:all) do

    set :backend, :docker
    set :docker_image, ENV['IMAGE_ID']
  end

  it "installs the right version of Centos" do
    expect(file('/etc/redhat-release')).to contain("CentOS")
  end

  packages = ['xorg-x11-server-Xvfb', 'xorg-x11-xinit',
              'chromedriver-2.22-1*', 'chromium-52.0.2743.0-1*',
              'firefox-45.0.2-1', 'dejavu-sans-fonts', 'dejavu-sans-mono-fonts',
              'dejavu-serif-fonts', 'phantomjs', 'maven-bin']

  it "installs required packages" do
    packages.each do |p|
      expect(package(p)).to be_installed
    end
  end

  it "can start Xvfb-run" do
    expect(command("xvfb-run echo 'test'").stdout).to eq "test\n"
  end
end
