
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
              'dejavu-sans-fonts', 'dejavu-sans-mono-fonts', 'gtk3-devel',
              'dejavu-serif-fonts', 'maven-bin']

  it "installs required packages" do
    packages.each do |p|
      expect(package(p)).to be_installed
    end
  end

  it "installs Firefox" do
    expect(command('firefox --version').stdout).to contain("Firefox 80.0")
  end

  it "installs Geckodriver" do
    expect(command('geckodriver --version').stdout).to contain("geckodriver")
  end

  it "can start Xvfb-run" do
    expect(command("xvfb-run echo 'test'").stdout).to eq "test\n"
  end

  it "ensure /etc/machine-id exists for D-Bus configuration" do
    expect(file('/etc/machine-id')).to contain("eff45cad5ee285945f958f2c7a1b9f64")
  end
end
