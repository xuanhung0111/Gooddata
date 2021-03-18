package com.gooddata.qa.utils.testng.listener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FilenameUtils;
import org.arquillian.extension.recorder.video.Video;
import org.arquillian.extension.recorder.video.desktop.configuration.DesktopVideoConfiguration;
import org.arquillian.extension.recorder.video.desktop.impl.DesktopVideoRecorder;
import org.arquillian.recorder.reporter.ReporterConfiguration;
import org.arquillian.recorder.reporter.impl.TakenResourceRegister;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class VideoRecorderListener extends TestListenerAdapter {

  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
  private final TakenResourceRegister reg = new TakenResourceRegister();
  private final DesktopVideoConfiguration config = new DesktopVideoConfiguration(new ReporterConfiguration());
  private final DesktopVideoRecorder recorder = new DesktopVideoRecorder(reg);
  boolean shouldRecordVideo;

  public VideoRecorderListener() {
    shouldRecordVideo = Boolean.valueOf(System.getProperty("video.recording"));
    recorder.init(config);
  }

  @Override
  public void onTestStart(ITestResult result) {
    if (!shouldRecordVideo) {
      System.out.println("onTestStart: not record videos");
      return;
    }
    // test_abc_2021_03_20_00_00_00.mp4
    String fileName = String.format("%s_%s",
        result.getMethod().getMethodName(), dateTimeFormatter.format(LocalDateTime.now()));
    File file = new File(result.getTestClass().getName(), fileName);
    try {
      recorder.startRecording(file);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void onTestFailure(ITestResult result) {
    if (!shouldRecordVideo) {
      System.out.println("onTestFailure: not record videos");
      return;
    }
    try {
      Video video = recorder.stopRecording();
      renameFailedTestVideo(video);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  @Override
  public void onTestSkipped(ITestResult result) {
    try {
      Video video = recorder.stopRecording();
      renameFailedTestVideo(video);
    } catch (IllegalStateException e) {
      // this exception throw if the test method depend on another method which failed
      // then no video recorded
      // but if the test skipped because an SkipException throw from inside the test, video still recorded
    }
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    if (!shouldRecordVideo) {
      System.out.println("onTestSuccess: not record videos");
      return;
    }
    try {
      recorder.stopRecording();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void renameFailedTestVideo(Video video) {
    try {
      Path path = video.getResource().toPath();
      // test_abc_2021_03_20_00_00_00_failed.mp4
      String newName = String.format("%s_failed.%s",
          FilenameUtils.getBaseName(video.getResource().getName()), FilenameUtils.getExtension(video.getResource().getName()));
      Files.move(video.getResource().toPath(), path.resolveSibling(newName));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
