package swiss.dasch.plugins.jcascqueuejob;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import hudson.Extension;
import hudson.model.BuildableItem;
import hudson.model.Cause;
import hudson.model.Item;
import hudson.model.Queue.QueueDecisionHandler;
import hudson.model.listeners.ItemListener;

@Extension
public class StartupItemListener extends ItemListener {

	@Override
	public void onLoaded() {
		StartupQueueDecisionHandler instance = QueueDecisionHandler.all().get(StartupQueueDecisionHandler.class);

		if (instance != null) {
			LinkedHashMap<BuildableItem, Cause> items = instance.getAndClearQueuedItems();

			for (Entry<BuildableItem, Cause> entry : items.entrySet()) {
				BuildableItem item = entry.getKey();
				Cause cause = entry.getValue();

				item.checkPermission(Item.BUILD);

				item.scheduleBuild(cause);
			}
		}
	}

}
