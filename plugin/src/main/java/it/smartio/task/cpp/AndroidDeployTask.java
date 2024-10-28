/**
 *
 */

package it.smartio.task.cpp;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import it.smartio.build.Build;
import it.smartio.common.env.Environment;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.TaskList;
import it.smartio.common.task.process.ProcessTask;
import it.smartio.task.file.CopyTask;
import it.smartio.task.property.PropertyTask;
import it.smartio.task.property.Replacer;
import it.smartio.util.version.Version;

/**
 * Defines a QMake task.
 */
public class AndroidDeployTask extends TaskList {

	private static final String SOURCE_APK = "$" + Build.BUILD_DIR
			+ "/%s/android-%s/%s/build/outputs/apk/release/%s-release-signed.apk";
	private static final String TARGET_APK = "$" + Build.BUILD_DIR + "/%s-$" + Build.REVISION + "-%s.apk";

	private static final String SOURCE_AAB = "$" + Build.BUILD_DIR
			+ "/%s/android/%s/build/outputs/bundle/release/%s-release.aab";
	private static final String TARGET_AAB = "$" + Build.BUILD_DIR + "/%s-$" + Build.REVISION + ".aab";

	private final String targetName;
	private final String moduleName;

	/**
	 * Creates an instance of {@link AndroidDeployTask}.
	 *
	 * @param targetName
	 * @param moduleName
	 */
	public AndroidDeployTask(String targetName, String moduleName) {
		this.targetName = targetName;
		this.moduleName = moduleName;
	}

	/**
	 * Gets the module name.
	 *
	 * @param env
	 */
	protected final String getTargetName() {
		return this.targetName;
	}

	protected final String getFileName(Environment env) {
		if (env.isSet(Build.PRODUCT_FILE) && env.get(Build.PRODUCT_FILE) != null) {
			return env.get(Build.PRODUCT_FILE);
		}
		return env.isSet(Build.PRODUCT_NAME) ? env.get(Build.PRODUCT_NAME) : getTargetName();
	}

	/**
	 * Gets the module name.
	 */
	protected final String getModuleName() {
		return this.moduleName;
	}

	@Override
	protected final void collect(List<Task> tasks, TaskContext context) {
		context.getLogger().onInfo("{} => {}", context.getEnvironment().get(Build.PRODUCT_NAME),
				context.getEnvironment().get(Build.PRODUCT_FILE));

		int buildNumber = Integer.parseInt(context.getEnvironment().get(Build.BUILDNUMBER));
		File buildPath = new File(context.getEnvironment().get(Build.BUILD_DIR), this.moduleName);
		String version = context.getEnvironment().get(Build.QT_VERSION);

		File qtRoot = new File(context.getEnvironment().get(Build.QT_ROOT));
		File qtHome = new File(qtRoot, version);
		try {
			tasks.add(new PropertyTask(new AndroidManifestReplacer(buildNumber)));

			if (Version.of(version).getMajor() > 5) {
				tasks.add(c -> handleDeploymentSettings(qtHome, buildPath, c));

				for (String abi : context.getEnvironment().get(Build.ANDROID_ABIS).split(",")) {
					File buildDir = new File(buildPath, "android-" + abi);

					String buildFile = "android-build";
					tasks.add(new AndroidDeploy(buildDir, buildFile, false));

					String sourceFilename = String.format(AndroidDeployTask.SOURCE_APK, getModuleName(), abi, buildFile,
							buildFile);
					String targetFilename = String.format(AndroidDeployTask.TARGET_APK,
							getFileName(context.getEnvironment()), abi);
					tasks.add(new CopyTask(sourceFilename, targetFilename));
				}
			}

			File buildDir = new File(buildPath, "android");
			String buildFile = "android-build";
			tasks.add(new AndroidDeploy(buildDir, buildFile, true));

			String sourceFilename = String.format(AndroidDeployTask.SOURCE_AAB, getModuleName(), buildFile, buildFile);
			String targetFilename = String.format(AndroidDeployTask.TARGET_AAB, getFileName(context.getEnvironment()));
			tasks.add(new CopyTask(sourceFilename, targetFilename));

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	protected void handleDeploymentSettings(File qtHome, File buildPath, TaskContext context) throws IOException {
		File buildDir = new File(buildPath, "android");
		File libsDir = new File(buildDir, "android-build/libs");
		libsDir.mkdirs();

		String[] abis = context.getEnvironment().get(Build.ANDROID_ABIS).split(",");
		for (String abi : abis) {
			File sourceDir = new File(buildPath, "android-" + abi);
			File targetDir = new File(libsDir, abi);
			targetDir.mkdirs();

			for (File f : sourceDir.listFiles(f -> f.getName().endsWith(".so"))) {
				Files.copy(f.toPath(), new File(targetDir, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}

		File source = new File(new File(buildPath, "android-" + abis[0]), "android-smartIO-deployment-settings.json");
		File target = new File(buildDir, "android-smartIO-deployment-settings.json");
		JsonObject json = Json.createReader(new FileReader(source)).readObject();
		JsonObjectBuilder builder = Json.createObjectBuilder();
		json.keySet().forEach(k -> builder.add(k, json.get(k)));

		try (JsonWriter writer = Json.createWriter(new FileWriter(target))) {
			JsonObjectBuilder qt = Json.createObjectBuilder();
			qt.add("arm64-v8a", new File(qtHome, "android_arm64_v8a").getAbsolutePath());
			qt.add("armeabi-v7a", new File(qtHome, "android_armv7").getAbsolutePath());
			builder.add("qt", qt.build());

			JsonObjectBuilder arch = Json.createObjectBuilder();
			arch.add("arm64-v8a", "aarch64-linux-android");
			arch.add("armeabi-v7a", "arm-linux-androideabi");
			builder.add("architectures", arch.build());

			writer.writeObject(builder.build());
		}
	}

	/**
	 * Creates a QAndroidDeploy process.
	 */
	private class AndroidDeploy extends ProcessTask {

		private final File buildDir;
		private final String installDir;
		private final boolean enableAab;

		/**
		 * @param buildDir
		 */
		public AndroidDeploy(File buildDir, String installDir, boolean enableAab) {
			this.buildDir = buildDir;
			this.installDir = installDir;
			this.enableAab = enableAab;
		}

		/**
		 * Get the QMake shell command.
		 */
		@Override
		protected AndroidBuilder getShellBuilder(TaskContext context) {
			File qtRoot = new File(context.getEnvironment().get(Build.QT_ROOT));
			File qtHome = new File(qtRoot, context.getEnvironment().get(Build.QT_ANDROID_DEPLOY));
			String keyStoreFile = context.getEnvironment().get(Build.ANDROID_KEYSTORE);

			AndroidBuilder builder = new AndroidBuilder(this.buildDir);
			builder.setHome(qtHome);
			builder.setBuildDir(this.installDir);
			builder.setTarget(getTargetName());
			if (this.enableAab) {
				builder.enableAAB();
			}
			builder.setJavaHome(context.getEnvironment().get(Build.ANDROID_JAVA_HOME));
			builder.setKeyStore(new File(context.getWorkingDir(), keyStoreFile));
			builder.setAlias(context.getEnvironment().get(Build.ANDROID_KEYSTORE_ALIAS));
			builder.setPassword(context.getEnvironment().get(Build.ANDROID_KEYSTORE_PASSWORD));
			return builder;
		}
	}

	private static class AndroidManifestReplacer extends Replacer {

		private final int buildnumber;

		public AndroidManifestReplacer(int buildnumber) {
			super("Manifest.xml", "((android:versionCode)=\")([^\"]+)(\")");
			this.buildnumber = buildnumber;
		}

		@Override
		public final String getValue(String name, String value, Environment environment) {
			switch (name) {
			case "android:versionCode":
				return "" + this.buildnumber;
			}
			return value;
		}
	}

}