
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
              'google-chrome-stable',
              'dejavu-sans-fonts', 'dejavu-sans-mono-fonts',
              'dejavu-serif-fonts', 'phantomjs', 'maven-bin']

  it "installs required packages" do
    packages.each do |p|
      expect(package(p)).to be_installed
    end
  end

  it "installs Firefox" do
    expect(command('firefox --version').stdout).to contain("Firefox")
  end

  it "installs Chrome" do
    expect(command('chrome --version').stdout).to contain("Google Chrome")
  end

  it "installs Geckodriver" do
    expect(command('geckodriver --version').stdout).to contain("geckodriver")
  end

  it "installs Chromedriver" do
    expect(command('chromedriver --version').stdout).to contain("ChromeDriver")
  end

  it "can start Xvfb-run" do
    expect(command("xvfb-run echo 'test'").stdout).to eq "test\n"
  end
end
