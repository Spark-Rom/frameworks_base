/*
 * Copyright (C) 2023 LibreMobileOS Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.libremobileos;

import static android.os.Process.THREAD_PRIORITY_DEFAULT;
import static android.hardware.biometrics.BiometricAuthenticator.TYPE_FACE;
import static android.hardware.biometrics.BiometricManager.Authenticators;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.IBiometricService;
import android.hardware.face.IFaceService;
import android.hardware.face.FaceManager;
import android.hardware.face.FaceSensorProperties;
import android.hardware.face.FaceSensorPropertiesInternal;
import android.hardware.face.IFaceAuthenticatorsRegisteredCallback;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintSensorPropertiesInternal;
import android.hardware.fingerprint.IFingerprintAuthenticatorsRegisteredCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;

import com.android.internal.R;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.biometrics.Utils;

import java.util.ArrayList;
import java.util.List;

import com.libremobileos.faceunlock.server.FaceUnlockServer;

public class FaceUnlockService extends SystemService {
    private static final String TAG = "FaceUnlockService";

    private ServiceThread mServiceThread = null;
    private FaceUnlockServer mServer = null;

    public FaceUnlockService(Context context) {
        super(context);
    }

    @Override
    public void onStart() {
        final PackageManager pm = getContext().getPackageManager();
        final boolean supportsFace = pm.hasSystemFeature(PackageManager.FEATURE_FACE);
        if (supportsFace) {
            mServiceThread = new ServiceThread(TAG, THREAD_PRIORITY_DEFAULT, false);
            mServiceThread.start();
            FaceManager faceManager = getContext().getSystemService(FaceManager.class);
            if (faceManager != null) {
                faceManager.addAuthenticatorsRegisteredCallback(
                    new IFaceAuthenticatorsRegisteredCallback.Stub() {
                        @Override
                        public void onAllAuthenticatorsRegistered(
                                List<FaceSensorPropertiesInternal> faceSensors) {
                            FingerprintManager fingerprintManager =
                                    getContext().getSystemService(FingerprintManager.class);
                            if (fingerprintManager != null) {
                                fingerprintManager.addAuthenticatorsRegisteredCallback(
                                    new IFingerprintAuthenticatorsRegisteredCallback.Stub() {
                                        @Override
                                        public void onAllAuthenticatorsRegistered(
                                                List<FingerprintSensorPropertiesInternal>
                                                fingerprintSensors) {
                                            initialize(faceSensors);
                                        }
                                    }
                                );
                            } else {
                                initialize(faceSensors);
                            }
                        }
                    }
                );
            } else {
                Slog.e(TAG, "Face feature exists, but FaceService is null.");
            }
        } else {
            Slog.i(TAG, "Not using any face sensor.");
        }
    }

    private void initialize(List<FaceSensorPropertiesInternal> faceSensors) {
        mServer = new FaceUnlockServer(getContext(), mServiceThread.getLooper(),
                this::publishBinderService);
        try {
            IBiometricService biometricService = IBiometricService.Stub.asInterface(
                ServiceManager.getService(Context.BIOMETRIC_SERVICE));
            if (biometricService == null) {
                Slog.e(TAG, "Face feature exists, but IBiometricService is null.");
                return;
            }
            IFaceService faceService = IFaceService.Stub.asInterface(
                ServiceManager.getService(Context.FACE_SERVICE));
            if (faceService == null) {
                Slog.e(TAG, "Face feature exists, but FaceService is null.");
                return;
            }
            if (faceSensors.size() == 0) {
                Slog.i(TAG, "Using software face sensor.");
                int newId = 0;
                // IDs may come from HALs and be non-linear, ensure we really have unique ID,
                // because if ID is duplicated, we crash system server!
                boolean foundDuplicate = false;
                do {
                    if (foundDuplicate) {
                        newId++;
                    }
                    foundDuplicate = biometricService.getCurrentStrength(newId)
                            != Authenticators.EMPTY_SET;
                } while (foundDuplicate);
                faceService.registerAuthenticators(getHidlFaceSensorProps(newId,
                        Authenticators.BIOMETRIC_STRONG));
            } else {
                Slog.i(TAG, "Using hardware face sensor.");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException when loading face configuration", e);
        }
    }

    private List<FaceSensorPropertiesInternal> getHidlFaceSensorProps(int sensorId,
            @BiometricManager.Authenticators.Types int strength) {
        // see AuthService.java getHidlFaceSensorProps()
        final boolean supportsSelfIllumination = getContext().getResources().getBoolean(
                R.bool.config_faceAuthSupportsSelfIllumination);
        final int maxTemplatesAllowed = getContext().getResources().getInteger(
                R.integer.config_faceMaxTemplatesPerUser);
        return List.of(new FaceSensorPropertiesInternal(sensorId,
                Utils.authenticatorStrengthToPropertyStrength(strength), maxTemplatesAllowed,
                new ArrayList<>(), FaceSensorProperties.TYPE_UNKNOWN, false,
                supportsSelfIllumination, true));
    }
}
