# react-native-google-ar-core

The Augmented Faces API lets you render assets on top of human faces without using specialized hardware. It offers feature points that allow your app to automatically identify different regions of a detected face. Your app can then use these regions to overlay assets in a way that matches the contours of an individual face.

Currently this library only supports Android. But we are accepting collaborators to integrate the IOS part.

## Support
For now it only support to Android. Help-us to improve!

## Installation

```sh
npm install react-native-google-ar-core
```

or

```sh
yarn add react-native-google-ar-core
```

## Usage

```js
import GoogleArCoreView, {
    capture as GoogleArCoreCapture,
    OnChangeEvent,
} from 'react-native-google-ar-core';

const App = () => {
    const onPress = async () => {
        const response = await GoogleArCoreCapture();
        if (response === true) {
            ToastAndroid.show('Captura Solicitada', 1000);
        } else {
            ToastAndroid.show('Captura Falhou', 1000);
        }
    };
    const onChange = (event: OnChangeEvent) => {
        console.log('OnChangeEvent', event);
    };
    return (
        <GoogleArCoreView onChange={onChange}>
            <View style={styles.mainContent}>
                <TouchableOpacity onPress={onPress}>
                    <Text style={styles.title}>Take picture</Text>
                </TouchableOpacity>
            </View>
        </GoogleArCoreView>
    );
};
```

## Props

Property | Type | Description
--- | --- | ---
children | Element | React Native Element max 2 primary childrens 
style | StyleProp | Camera Style
onChange | Function | Método acionado quando o método capture termina de ser executado
onFailedCapture | Function | Método acionado quando o método capture falha
## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
