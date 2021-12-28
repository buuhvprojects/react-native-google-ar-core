/* eslint-disable react-native/no-inline-styles */
import React from 'react';
import {
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

type GoogleArCoreViewProps = {
    children?: Element[] | Element;
    style?: StyleProp<ViewStyle>;
};

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

const CustomView =
    UIManager.getViewManagerConfig(ComponentName) != null
        ? requireNativeComponent<GoogleArCoreViewProps>(ComponentName)
        : () => {
              throw new Error(LINKING_ERROR);
          };

export const capture = async (): Promise<void> => {
    return await GoogleArCore.capture();
};

const GoogleArCoreView = (props: GoogleArCoreViewProps) => {
    return <CustomView {...props} style={[{ flex: 1 }, props.style]} />;
};

export default GoogleArCoreView;
