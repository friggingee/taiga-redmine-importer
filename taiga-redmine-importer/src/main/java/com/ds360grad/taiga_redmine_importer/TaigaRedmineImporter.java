package com.ds360grad.taiga_redmine_importer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;

import net.kaleidos.domain.IssueStatus;
import net.kaleidos.domain.Project;
import net.kaleidos.taiga.TaigaClient;

public class TaigaRedmineImporter {
	private static String redmineUri = "your-redmine-uri";
	private static String redmineApiAccessKey = "your-redmine-api-key";
	private static String redmineProjectKey = null;
	private static Integer redmineQueryId = null; // any

	private static TaigaClient taigaClient = new TaigaClient(
			"http://taiga.local");

	public static void removeTaigaProjects() {
		List<Project> taigaProjects = taigaClient.getProjects();

		for (Project taigaProject : taigaProjects) {
			taigaClient.deleteProject(taigaProject);
		}
	}

	public static void main(String[] args) {
		taigaClient.authenticate("your-redmine-username",
				"your-redmine-password");

		TaigaRedmineImporter.removeTaigaProjects();

		RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey(
				redmineUri, redmineApiAccessKey);

		List<Issue> redmineIssues = null;
		List<com.taskadapter.redmineapi.bean.Project> redmineProjects = null;

		try {

			redmineProjects = redmineManager.getProjectManager().getProjects();

			for (com.taskadapter.redmineapi.bean.Project redmineProject : redmineProjects) {
				redmineProjectKey = redmineProject.getIdentifier();
				List<String> taigaDefaultPriorities = Arrays.asList(
					"High",
					"Normal",
					"Low"
				);
				List<String> taigaDefaultSeverities = Arrays.asList(
					"Wishlist",
					"Minor",
					"Normal",
					"Important",
					"Critical"
				);
				List<String> taigaDefaultIssueTypes = Arrays.asList(
					"Bug",
					"Question",
					"Enhancement",
					"Feature",
					"Support"
				);
				List<IssueStatus> taigaDefaultStatuses = Arrays.asList(
					new IssueStatus().setName("New").setIsClosed(false),
					new IssueStatus().setName("In progress").setIsClosed(false),
					new IssueStatus().setName("Ready for test").setIsClosed(false),
					new IssueStatus().setName("Closed").setIsClosed(true),
					new IssueStatus().setName("Needs Info").setIsClosed(false),
					new IssueStatus().setName("Rejected").setIsClosed(true),
					new IssueStatus().setName("Postponed").setIsClosed(false)
				);
				String redmineProjectName = redmineProject.getName();

				Project taigaProject = new Project()
						.setName(redmineProjectName)
						.setDescription(redmineProjectName)
						.setSlug(redmineProjectKey)
						.setIssuePriorities(taigaDefaultPriorities)
						.setIssueSeverities(taigaDefaultSeverities)
						.setIssueTypes(taigaDefaultIssueTypes)
						.setIssueStatuses(taigaDefaultStatuses);

				taigaProject = taigaClient.createProject(taigaProject);

				redmineIssues = redmineManager.getIssueManager().getIssues(
						redmineProjectKey, redmineQueryId);

				for (Issue redmineIssue : redmineIssues) {
					net.kaleidos.domain.Issue taigaIssue = new net.kaleidos.domain.Issue()
							.setProject(taigaProject)
							.setDescription(redmineIssue.getDescription())
							.setSubject(redmineIssue.getSubject())
							.setSeverity("Normal").setType("Bug")
							.setStatus("New").setPriority("Normal");

					taigaClient.createIssue(taigaIssue);

					// System.out.println(redmineIssue.getId()
					// + " // (" + redmineIssue.getTracker().getId() + ": " +
					// redmineIssue.getTracker().getName() + ")"
					// + " // (" + redmineIssue.getStatusId() + ": " +
					// redmineIssue.getStatusName() + ")"
					// + " // " + redmineIssue.getPriorityId() + ": " +
					// redmineIssue.getPriorityText());
					// System.out.println();
				}
			}
		} catch (RedmineException e) {
			e.printStackTrace();
		}
	}
}