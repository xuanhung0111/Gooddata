package com.gooddata.qa.graphene.enums.disc;

import java.util.ArrayList;
import java.util.List;

public enum DeployPackages {

    BASIC("Basic", "Basic", ProcessTypes.GRAPH),
    ONE_GRAPH("One_Graph", "One_Graph", ProcessTypes.GRAPH),
    ONE_GRAPH_RENAMED("One_Graph_Renamed", "One_Graph", ProcessTypes.GRAPH),
    CLOUDCONNECT("cloudconnect", "DWHS", ProcessTypes.GRAPH),
    RUBY("ruby", "ruby", ProcessTypes.RUBY),
    EXECUTABLES_GRAPH("executables", "executables", ProcessTypes.GRAPH),
    EXECUTABLES_RUBY("executables", "executables", ProcessTypes.RUBY),
    CTL_EVENT("CTL_event", "CTL_event", ProcessTypes.GRAPH),
    NOT_EXECUTABLE("not-executables");

    private String packageName;
    private String rootFolder;
    private ProcessTypes packageType;

    private DeployPackages(String packageName, String rootFolder, ProcessTypes packageType) {
        this.packageName = packageName;
        this.rootFolder = rootFolder;
        this.packageType = packageType;
    }

    private DeployPackages(String packageName) {
        this(packageName, null, null);
    }

    public String getPackageName() {
        return this.packageName + ".zip";
    }

    public List<Executables> getExecutables() {
        List<Executables> executableList = new ArrayList<Executables>();
        for (Executables executable : Executables.values()) {
            if (executable.getExecutablePackage() == this)
                executableList.add(executable);
        }
        return executableList;
    }

    public String getPackageRootFolder() {
        return this.rootFolder;
    }

    public ProcessTypes getPackageType() {
        return packageType;
    }

    public enum Executables {
        FAILED_GRAPH(
                DeployPackages.BASIC,
                "errorGraph.grf",
                "graph",
                "Graph=Basic/graph/errorGraph.grf error: Graph=schedule_param transformation failed!: " +
                        "Component [GD Dataset Writer:GD_DATASET_WRITER] finished with status ERROR."),
        LONG_TIME_RUNNING_GRAPH(DeployPackages.BASIC, "longTimeRunningGraph.grf", "graph"),
        SHORT_TIME_FAILED_GRAPH(
                DeployPackages.BASIC,
                "shortTimeErrorGraph.grf",
                "graph",
                "Graph=Basic/graph/shortTimeErrorGraph.grf error: Graph=schedule_param transformation failed!: "
                        + "Component [GD Dataset Writer:GD_DATASET_WRITER] finished with status ERROR.: "
                        + "Unrecoverable error in SLI upload occurred."),
        SUCCESSFUL_GRAPH(DeployPackages.BASIC, "successfulGraph.grf", "graph"),
        SUCCESSFUL_GRAPH_FOR_BROKEN_SCHEDULE(DeployPackages.ONE_GRAPH, "successfulGraph.grf", "graph"),
        SUCCESSFUL_GRAPH_FOR_BROKEN_SCHEDULE_RENAMED(DeployPackages.ONE_GRAPH_RENAMED, "successfulGraph_Rename.grf", "graph"),
        DWHS1(DeployPackages.CLOUDCONNECT, "DWHS1.grf", "graph"),
        DWHS2(DeployPackages.CLOUDCONNECT, "DWHS2.grf", "graph"),
        RUBY1(DeployPackages.RUBY, "ruby1.rb", "script"),
        RUBY2(DeployPackages.RUBY, "ruby2.rb", "script"),
        EXECUTABLES_GRAPH1(DeployPackages.EXECUTABLES_GRAPH, "01 - REST_GET_1.grf", "graph"),
        EXECUTABLES_GRAPH2(DeployPackages.EXECUTABLES_GRAPH, "02 - REST_POST.grf", "graph"),
        EXECUTABLES_GRAPH3(DeployPackages.EXECUTABLES_GRAPH, "03 - REST_PUT.grf", "graph"),
        EXECUTABLES_GRAPH4(DeployPackages.EXECUTABLES_GRAPH, "04 - REST_DELETE.grf", "graph"),
        EXECUTABLES_RUBY1(DeployPackages.EXECUTABLES_RUBY, "ruby1.rb", "script"),
        EXECUTABLES_RUBY2(DeployPackages.EXECUTABLES_RUBY, "ruby2.rb", "script"),
        CTL_GRAPH(DeployPackages.CTL_EVENT, "CTL_Function.grf", "graph");

        private String executable;
        private String executableFolder;
        private String errorMessage;
        private DeployPackages deployPackage;

        private Executables(DeployPackages deployPackage, String executable,
                String executableFolder, String errorMessage) {
            this.deployPackage = deployPackage;
            this.executable = executable;
            this.executableFolder = executableFolder;
            this.errorMessage = errorMessage;
        }

        private Executables(DeployPackages deployPackage, String executable, String executableFolder) {
            this(deployPackage, executable, executableFolder, "");
        }

        public DeployPackages getExecutablePackage() {
            return deployPackage;
        }

        public String getExecutablePath() {
            return "/" + executableFolder + "/" + executable;
        }

        public String getExecutableName() {
            return this.executable;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }
    }
}
