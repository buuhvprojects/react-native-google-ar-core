import { Platform, requireNativeComponent, UIManager } from 'react-native';

const LINKING_ERROR =
    `The package 'react-native-google-ar-core' doesn't seem to be linked. Make sure: \n\n` +
    Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
    '- You rebuilt the app after installing the package\n' +
    '- You are not using Expo managed workflow\n';

type GoogleArCoreViewProps = {
    children?: Element[] | Element;
};

// const GoogleArCore = NativeModules.GoogleArCore
//     ? NativeModules.GoogleArCore
//     : new Proxy(
//           {},
//           {
//               get() {
//                   throw new Error(LINKING_ERROR);
//               },
//           }
//       );

const ComponentName = 'GoogleArCoreView';

const GoogleArCoreView =
    UIManager.getViewManagerConfig(ComponentName) != null
        ? requireNativeComponent<GoogleArCoreViewProps>(ComponentName)
        : () => {
              throw new Error(LINKING_ERROR);
          };
export default GoogleArCoreView;
