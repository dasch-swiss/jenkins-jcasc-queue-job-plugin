package swiss.dasch.plugins.jcascqueuejob;

import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nullable;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Action;
import hudson.model.BuildableItem;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Queue.QueueDecisionHandler;
import hudson.model.Queue.Task;
import hudson.security.ACL;
import jenkins.model.Jenkins;

@Extension
public class StartupQueueDecisionHandler extends QueueDecisionHandler {

	private static final String JOB_DSL_CAUSE_CLASS = "javaposse.jobdsl.plugin.JenkinsJobManagement$JobDslCause";

	private static boolean isLoading;

	@Restricted(NoExternalUse.class)
	@Initializer(before = InitMilestone.SYSTEM_CONFIG_LOADED)
	public static void onBeforeSystemConfigLoaded() {
		isLoading = true;
	}

	@Restricted(NoExternalUse.class)
	@Initializer(after = InitMilestone.JOB_CONFIG_ADAPTED)
	public static void onAfterJobConfigAdapted() {
		isLoading = false;
	}

	private LinkedHashMap<BuildableItem, Cause> queuedItems = new LinkedHashMap<>();

	public LinkedHashMap<BuildableItem, Cause> getAndClearQueuedItems() {
		LinkedHashMap<BuildableItem, Cause> items;

		synchronized (this) {
			items = this.queuedItems;
			this.queuedItems = new LinkedHashMap<>();
		}

		return items;
	}

	@Override
	public boolean shouldSchedule(Task task, List<Action> actions) {
		if (isLoading && Jenkins.getAuthentication2() == ACL.SYSTEM2 && task instanceof BuildableItem) {
			Cause cause = getJobDSLCause(actions);
			if (cause != null) {
				// Task was queued by JobDSL during initialization. Cancel scheduling for
				// now and schedule it again later once Jenkins Queue has finished loading.
				synchronized (this) {
					this.queuedItems.put((BuildableItem) task, cause);
				}
				return false;
			}
		}
		return true;
	}

	@Nullable
	private Cause getJobDSLCause(List<Action> actions) {
		for (Action action : actions) {
			if (action instanceof CauseAction) {
				CauseAction causeAction = (CauseAction) action;
				for (Cause cause : causeAction.getCauses()) {
					if (JOB_DSL_CAUSE_CLASS.equals(cause.getClass().getName())) {
						return cause;
					}
				}
			}
		}
		return null;
	}

}
