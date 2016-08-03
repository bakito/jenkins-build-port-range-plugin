package ch.bakito.jenkins.plugin.bpr;

import java.io.IOException;
import java.util.Map;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapper;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONObject;

/**
 * BuildPortRangeBuildWrapper
 */
public class BuildPortRangeBuildWrapper extends SimpleBuildWrapper {

  private final Integer portPoolSize;

  private Range range = null;

  /**
   * BuildPortRangeBuildWrapper
   * @param portPoolSize the portPoolSize
   */
  @DataBoundConstructor
  public BuildPortRangeBuildWrapper(Integer portPoolSize) {
    super();
    this.portPoolSize = portPoolSize;
  }

  public Integer getPortPoolSize() {
    return portPoolSize;
  }

  @Override
  public void setUp(Context context, Run<?, ?> run, FilePath filePath, Launcher launcher, TaskListener taskListener, EnvVars envVars) throws IOException, InterruptedException {

    if (portPoolSize != null) {
      DescriptorImpl descriptor = getDescriptor();
      range = descriptor.getRange(run.getId(), portPoolSize);
    }

    context.setDisposer(new Disposer() {
      @Override
      public void tearDown(Run<?, ?> run, FilePath filePath, Launcher launcher, TaskListener taskListener) throws IOException, InterruptedException {
        if (portPoolSize != null) {
          DescriptorImpl descriptor = getDescriptor();
          descriptor.releaseRange(run.getId());
        }
      }
    });
  }

  @Override
  public void makeBuildVariables(AbstractBuild build, Map<String, String> variables) {
    super.makeBuildVariables(build, variables);

    if (range != null) {
      DescriptorImpl descriptor = getDescriptor();
      for (int i = range.getFrom(); i <= range.getTo(); i++) {
        variables.put(descriptor.ENV_VAR_PREFIX + "_" + (i - range.getFrom()), String.valueOf(i));
      }
    }

  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<BuildWrapper> {

    private static final String ENV_VAR_PREFIX = "PORT_POOL";
    private int startPort = 50000;
    private int poolSize = 1000;

    private transient String[] pool = new String[0];

    public DescriptorImpl() {
      load();
      init();
    }

    public Range getRange(String jobId, int ports) throws AbortException {
      if (ports > pool.length) {
        throw new AbortException("No free ports available in port range. Start port: " + startPort + " pool size: " + poolSize);
      }
      synchronized (pool) {

        int startIndex = 0;
        int cnt = 0;
        for (int i = 0; i < pool.length; i++) {
          if (pool[i] == null) {
            if (startIndex < 0) {
              startIndex = i;
            }

            cnt++;
            if (cnt == ports) {
              break;
            }
          } else {
            if (cnt < ports) {
              startIndex = -1;
              cnt = 0;
            }
          }
          if (i > poolSize) {
            throw new AbortException("No free ports available in port range. Start port: " + startPort + " pool size: " + poolSize);
          }

        }
        for (int i = startIndex; i < startIndex + ports; i++) {
          pool[i] = jobId;
        }
        return new Range(startPort + startIndex, startPort + startIndex + ports - 1);
      }
    }

    public void releaseRange(String jobName) {
      synchronized (pool) {
        for (int i = 0; i < pool.length; i++) {
          if (jobName.equals(pool[i])) {
            pool[i] = null;
          }
        }
      }
    }

    private synchronized void init() {
      pool = new String[poolSize];
    }

    @Override
    public String getDisplayName() {
      return "Build Port Range Configuration";
    }

    /**
     * @return the startPort
     */
    public int getStartPort() {
      return startPort;
    }

    /**
     * @param startPort the startPort to set
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
     * @param poolSize the poolSize to set
     */
    public void setPoolSize(int poolSize) {
      this.poolSize = poolSize;
      init();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
      req.bindJSON(this, formData);
      save();
      return true;
    }
  }

  private static final class Range {

    private final int from;
    private final int to;

    public Range(int from, int to) {
      this.from = from;
      this.to = to;
    }

    public int getFrom() {
      return from;
    }

    public int getTo() {
      return to;
    }
  }
}
