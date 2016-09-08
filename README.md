carbon-business-process
=======================

---

|  Branch | Build Status |
| :------------ |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/job/carbon-business-process/badge/icon)](https://wso2.org/jenkins/job/carbon-business-process/badge) |


---

This project contains the modules implementing BPEL , WS-Human Tasks and BPMN support for WSO2 Business Process Server Product.
The project structure consist of service-stubs, components and features.

In the service-stubs section, services providing admin service API's and their respective clients are implemented. These are used
by the product ui.

In the components section, components corresponding to following functionality are avaialble. 

1. BPEL  - Apache ode integration layer and UI
2. Human Tasks - Human Tasks engine and respective UI
3. BPMN - Activiti integration layer and UI

In the features section , these individual components are packaged into features as bpel , human tasks and bpmn.

