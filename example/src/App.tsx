/* eslint-disable prettier/prettier */
import React, { useState } from 'react';
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
    const [effectIndex, setEffectIndex] = useState(0);
    
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
    const onChangeEffect = () => {
        setEffectIndex(effectIndex < 3 ? effectIndex + 1 : 0);
    }
    return (
        <GoogleArCoreView
            onChange={onChange}
            imagesDir='/MyApp'
            effectIndex={effectIndex}
            onFailedCapture={onFailedCapture}>
            <View style={styles.mainContent}>
                <TouchableOpacity onPress={onPress} style={styles.button}>
                    <Text style={styles.title}>Tirar foto</Text>
                </TouchableOpacity>
                <TouchableOpacity onPress={onChangeEffect} style={styles.button}>
                    <Text style={styles.title}>Alternar</Text>
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
        flexDirection: 'column',
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
    button: {
        marginBottom: 10,
    }
});

export default App;
