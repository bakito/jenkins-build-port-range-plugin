package ch.bakito.jenkins.plugin.bpr;

import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Plugin;
import hudson.model.Descriptor.FormException;

public class PortPoolPlugin extends Plugin {

	/**
	 * @see hudson.Plugin#start()
	 */
	@Override
	public void start() throws Exception {
		super.start();
	}

	/**
	 * @see hudson.Plugin#stop()
	 */
	@Override
	public void stop() throws Exception {
		super.stop();
	}

	/**
	 * @see hudson.Plugin#configure(org.kohsuke.stapler.StaplerRequest, net.sf.json.JSONObject)
	 */
	@Override
	public void configure(StaplerRequest req, JSONObject formData)
			throws IOException, ServletException, FormException {
		super.configure(req, formData);
	}

	/**
	 * @see hudson.Plugin#load()
	 */
	@Override
	protected void load() throws IOException {
		super.load();
	}

	/**
	 * @see hudson.Plugin#save()
	 */
	@Override
	public void save() throws IOException {
		super.save();
	}

}
