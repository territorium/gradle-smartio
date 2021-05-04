/*
 * Copyright (c) 2001-2021 Territorium Online Srl / TOL GmbH. All Rights Reserved.
 *
 * This file contains Original Code and/or Modifications of Original Code as defined in and that are
 * subject to the Territorium Online License Version 1.0. You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at http://www.tol.info/license/
 * and read it before using this file.
 *
 * The Original Code and all software distributed under the License are distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TERRITORIUM ONLINE HEREBY
 * DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. Please see the License for
 * the specific language governing rights and limitations under the License.
 */

package it.smartio.build;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.jaxrs.JerseyDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

/**
 * The {@link DockerMain} class.
 */
public class DockerMain {


  /**
   * {@link #main}.
   *
   * @see https://github.com/docker-java/docker-java/blob/master/docs/getting_started.md
   *
   * @param args
   */
  public static void main(String[] args) {
    DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

    DockerHttpClient httpClient = new JerseyDockerHttpClient.Builder().dockerHost(config.getDockerHost())
        .sslConfig(config.getSSLConfig()).build();

    DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
    dockerClient.pingCmd().exec();

    dockerClient.listContainersCmd().exec().forEach(c -> System.out.println(c.getImage()));


    // DockerClientConfig custom =
    // DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost("tcp://localhost:3500")
    // .withDockerTlsVerify(true).withDockerCertPath("/home/brigl/.docker").withRegistryUsername(registryUser)
    // .withRegistryPassword(registryPass).withRegistryEmail(registryMail).withRegistryUrl(registryUrl).build();
    //
  }
}
