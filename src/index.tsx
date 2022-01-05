/* eslint-disable prettier/prettier */
/* eslint-disable react-native/no-inline-styles */
import React, { useCallback, useEffect } from 'react';
import {
    DeviceEventEmitter,
    NativeModules,
    Platform,
    requireNativeComponent,
    StyleProp,
    UIManager,
    ViewStyle,
} from 'react-native';

const LINKING_ERROR =
    `The package 'react-native-google-ar-core' doesn't seem to be linked. Make sure: \n\n` +
    Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
    '- You rebuilt the app after installing the package\n' +
    '- You are not using Expo managed workflow\n';

export type OnChangeEvent = {
    filePath: string;
};
export type OnFailedCapture = {
    [key: string]: string | number;
};

interface GoogleArCoreViewProps {
    children?: Element[] | Element;
    style?: StyleProp<ViewStyle>;
    /**
     * Método acionado quando o método capture termina de ser executado
     * @todo esse método retorna um objeto com o filepath da imagem salva
     */
    onChange?: (event: OnChangeEvent) => void;
    /**
     * Método acionado quando o método capture falha
     * @todo esse método retorna um objeto com o erro
     */
     onFailedCapture?: (event: OnFailedCapture) => void;
    /**
     * Pasta no dispositivo onde serão salvas as imagens. As imagens são salvas por padrão dento de Pictures. A pasta que você definir, será criada dentro de Pictures.
     * @example imagesDir='myapp/media'
     */
    imagesDir?: string;
    /**
     * Efeito a ser aplicada
     */
    effectIndex?: number;
};
export type RecordingStatus = 'STARTED' | 'STOPPED' | 'FAILED';

const GoogleArCore = NativeModules.GoogleArCore
    ? NativeModules.GoogleArCore
    : new Proxy(
          {},
          {
              get() {
                  throw new Error(LINKING_ERROR);
              },
          }
      );

const ComponentName = 'GoogleArCoreView';

/**
 * Captura uma imagem da câmera
 * @returns Promise<boolean>
 */
export const capture = async (): Promise<boolean> => {
    return await GoogleArCore.capture();
};
/**
 * Inicia a gravação da câmera
 * @returns Promise<boolean>
 */
export const startRecording = async (): Promise<boolean> => {
    return await GoogleArCore.startRecording();
}
/**
 * Encerra a gravação da câmera
 * @returns Promise<boolean>
 */
export const stopRecording = async (): Promise<boolean> => {
    return await GoogleArCore.stopRecording();
}
/**
 * Determina o status atual da gravação
 * @returns Promise<boolean>
 */
export const getRecordingStatus = async (): Promise<RecordingStatus> => {
    return await GoogleArCore.getRecordingStatus();
}
/**
 * Pausa o ARCore
 * @returns Promise<boolean>
 */
export const pauseSession = async (): Promise<boolean> => {
    return await GoogleArCore.pauseSession();
}
/**
 * Continua o ARCore
 * @returns Promise<boolean>
 */
export const resumeSession = async (): Promise<boolean> => {
    return await GoogleArCore.resumeSession();
}
/**
 * Encerra o ARCore
 * @returns Promise<boolean>
 */
export const stopSession = async (): Promise<boolean> => {
    return await GoogleArCore.stopSession();
}
const isNullComponent = () => {
    return UIManager.getViewManagerConfig(ComponentName) != null;
}
const renderComponent = () => {
    return requireNativeComponent<GoogleArCoreViewProps>(ComponentName);
}
const errorComponent = () => {
    throw new Error(LINKING_ERROR);
}
const CustomView = isNullComponent() ? renderComponent() : errorComponent();

const GoogleArCoreView = (props: GoogleArCoreViewProps) => {
    const _onChange = useCallback(
        (data) => {
            if (props.onChange) {
                props.onChange(data);
            }
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [props.onChange]
    );
    const _onFailedCapture = useCallback(
        (data) => {
            if (props.onFailedCapture) {
                props.onFailedCapture(data);
            }
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [props.onFailedCapture]
    );
    useEffect(() => {
        DeviceEventEmitter.addListener('onChange', _onChange);
        DeviceEventEmitter.addListener('onFailedCapture', _onFailedCapture);
        return () => {
            DeviceEventEmitter.removeListener('onChange', _onChange);
            DeviceEventEmitter.removeListener('onFailedCapture', _onFailedCapture);
        }
    }, [_onChange, _onFailedCapture]);
    return (
        <CustomView
            {...props}
            style={[{ flex: 1 }, props.style]}
        />
    );
};

export default GoogleArCoreView;
