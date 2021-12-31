/* eslint-disable prettier/prettier */
import React from 'react';
import {
    View,
    StyleSheet,
    Text,
    TouchableOpacity,
    ToastAndroid,
} from 'react-native';
import GoogleArCoreView, {
    capture as GoogleArCoreCapture,
    OnChangeEvent,
    OnFailedCapture,
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
    const onFailedCapture = (event: OnFailedCapture) => {
        console.log('OnFailedCapture', event);
    }
    return (
        <GoogleArCoreView
            showFaceMakeup={false}
            showLeftEar={true}
            showNose={false}
            showRightEar={true}
            onChange={onChange}
            imagesDir='/MyApp'
            onFailedCapture={onFailedCapture}>
            <View style={styles.mainContent}>
                <TouchableOpacity onPress={onPress}>
                    <Text style={styles.title}>Tirar foto</Text>
                </TouchableOpacity>
            </View>
        </GoogleArCoreView>
    );
};

const styles = StyleSheet.create({
    mainContent: {
        flex: 1,
        backgroundColor: 'transparent',
        alignItems: 'center',
        justifyContent: 'center',
        alignContent: 'center',
    },
    title: {
        fontSize: 40,
        color: '#000',
        alignSelf: 'center',
        backgroundColor: '#FFF',
        borderRadius: 4,
        borderWidth: 1,
        paddingLeft: 10,
        paddingRight: 10,
    },
    bottomSheet: {
        backgroundColor: '#202122',
        alignItems: 'center',
    },
    sheetTitle: {
        marginTop: 16,
        color: '#fff',
        fontSize: 26,
    },
});

export default App;
