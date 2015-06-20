package ch.bakito.jenkins.plugin.bpr;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildWrapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

@SuppressWarnings("rawtypes")
public class PoolConfig extends BuildWrapper {

	private final Integer ports;

	/**
	 * 
	 */
	@DataBoundConstructor
	public PoolConfig(Integer ports) {
		super();
		this.ports = ports;
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {

		if (ports != null) {
			DescriptorImpl descriptor = getDescriptor();
			descriptor.getRange(build.getId(), ports);
		}

		return new PoolConfigEnvironment();
	}

	public class PoolConfigEnvironment extends Environment {
		@Override
		public boolean tearDown(AbstractBuild build, BuildListener listener)
				throws IOException, InterruptedException {
			System.err.println("tearDown");
			return super.tearDown(build, listener);
		}
	}

	@Override
	public void makeBuildVariables(AbstractBuild build,
			Map<String, String> variables) {
		super.makeBuildVariables(build, variables);
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static class DescriptorImpl extends Descriptor<BuildWrapper> {

		private String envVarPrefix = "PORT_POOL";
		private int startPort = 50000;
		private int poolSize = 1000;

		private String[] pool = new String[0];

		public DescriptorImpl() {
			init();
		}

		public List<Integer> getRange(String jobId, int ports) {
			synchronized (pool) {

				int startIndex = 0;
				int cnt = 0;
				for (int i = 0; i < pool.length; i++) {
					if (pool[i] == null) {
						if (startIndex == 0) {
							startIndex = i;
						}

						cnt++;
						if (cnt == ports) {
							break;
						}
					} else {
						if (cnt < ports) {
							startIndex = 0;
							cnt = 0;
						}
					}

				}

				return null;
			}
		}

		public void releaseRange(String jobName) {
			synchronized (pool) {

			}
		}

		private synchronized void init() {
			pool = new String[poolSize];
		}

		@Override
		public String getDisplayName() {
			return "PoolConfig";
		}

		/**
		 * @return the startPort
		 */
		public int getStartPort() {
			return startPort;
		}

		/**
		 * @param startPort
		 *            the startPort to set
		 */
		public void setStartPort(int startPort) {
			this.startPort = startPort;
		}

		/**
		 * @return the poolSize
		 */
		public int getPoolSize() {
			return poolSize;
		}

		/**
		 * @param poolSize
		 *            the poolSize to set
		 */
		public void setPoolSize(int poolSize) {
			this.poolSize = poolSize;
			init();
		}

		/**
		 * @return the envVarPrefix
		 */
		public String getEnvVarPrefix() {
			return envVarPrefix;
		}

		/**
		 * @param envVarPrefix
		 *            the envVarPrefix to set
		 */
		public void setEnvVarPrefix(String envVarPrefix) {
			this.envVarPrefix = envVarPrefix;
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws FormException {
			req.bindJSON(this, formData);
			save();
			return super.configure(req, formData);
		}
	}

}
