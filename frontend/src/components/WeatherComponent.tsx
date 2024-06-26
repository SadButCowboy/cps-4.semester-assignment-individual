import React from 'react'
import './WeatherComponent.css'
interface WeatherComponentProps{
    value: number | string | undefined;
    valueName: string;
    imagePath:string;
    altText: string;
    unit:string;
}
const WeatherComponent = ({value, valueName, imagePath, altText,unit} : WeatherComponentProps ) => {
    return (
        <div className='weather-component'>
            <span className='icon-name'>
                <img height={50} className='weather-icon' src={imagePath} alt={altText}></img>
                <h3>{valueName} </h3>
            </span>
            <h3>{value!=null ? value : '0.0'} {unit}</h3>
        </div>
    )
}

export default WeatherComponent