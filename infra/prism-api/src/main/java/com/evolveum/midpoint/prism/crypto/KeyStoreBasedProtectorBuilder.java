/*
 * Copyright (c) 2010-2018 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.prism.crypto;

import org.jetbrains.annotations.NotNull;

import javax.net.ssl.TrustManager;
import java.util.List;

/**
 *  Builder for KeyStoreBasedProtector implementation.
 */
public final class KeyStoreBasedProtectorBuilder {
    private String keyStorePath;
    private String keyStorePassword;
    private String encryptionKeyAlias;
    private String requestedJceProviderName;
    private String encryptionAlgorithm;
    private String digestAlgorithm;
    private List<TrustManager> trustManagers;

    @NotNull private final ProtectorCreator protectorCreator;

    public static KeyStoreBasedProtectorBuilder create(@NotNull ProtectorCreator protectorCreator) {
        return new KeyStoreBasedProtectorBuilder(protectorCreator);
    }

    private KeyStoreBasedProtectorBuilder(@NotNull ProtectorCreator protectorCreator) {
        this.protectorCreator = protectorCreator;
    }

    public KeyStoreBasedProtectorBuilder keyStorePath(String val) {
        keyStorePath = val;
        return this;
    }

    public KeyStoreBasedProtectorBuilder keyStorePassword(String val) {
        keyStorePassword = val;
        return this;
    }

    public KeyStoreBasedProtectorBuilder encryptionKeyAlias(String val) {
        encryptionKeyAlias = val;
        return this;
    }

    public KeyStoreBasedProtectorBuilder requestedJceProviderName(String val) {
        requestedJceProviderName = val;
        return this;
    }

    public KeyStoreBasedProtectorBuilder encryptionAlgorithm(String val) {
        encryptionAlgorithm = val;
        return this;
    }

    public KeyStoreBasedProtectorBuilder digestAlgorithm(String val) {
        digestAlgorithm = val;
        return this;
    }

    public KeyStoreBasedProtectorBuilder trustManagers(List<TrustManager> val) {
        trustManagers = val;
        return this;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getEncryptionKeyAlias() {
        return encryptionKeyAlias;
    }

    public String getRequestedJceProviderName() {
        return requestedJceProviderName;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public List<TrustManager> getTrustManagers() {
        return trustManagers;
    }

    public Protector initialize() {
        return protectorCreator.createInitializedProtector(this);
    }

    /**
     * Creates the protector without actually initializing it.
     */
    public Protector buildOnly() {
        return protectorCreator.createProtector(this);
    }
}
