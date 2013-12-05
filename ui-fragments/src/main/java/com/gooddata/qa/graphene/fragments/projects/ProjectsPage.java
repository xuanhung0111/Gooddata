package com.gooddata.qa.graphene.fragments.projects;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ProjectsPage extends AbstractFragment {
	
	@FindBy(xpath="//ul[@id='myProjects']/li")
	private List<WebElement> projects;
	
	private static final By BY_SPAN_PROJECT_TITLE = By.xpath("span[@class='projectTitle']");
	
	public List<WebElement> getProjectsElements() {
		return projects;
	}
	
	public List<String> getProjectsIds() {
		return getProjectsIds(null);
	}
	
	public List<String> getProjectsIds(String projectSubstringFilter) {
		List<String> projectIds = new ArrayList<String>();
		boolean filter = projectSubstringFilter != null && projectSubstringFilter.length() > 0;
		for (WebElement elem : projects) {
			if (filter) {
				if (!elem.findElement(BY_SPAN_PROJECT_TITLE).getText().contains(projectSubstringFilter)) continue;
			}
			String gdcLink = elem.getAttribute("gdc:link");
			projectIds.add(gdcLink.substring(gdcLink.lastIndexOf("/") + 1));
		}
		return projectIds;
	}

}
