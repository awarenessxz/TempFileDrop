import moment from 'moment';

// format 2020-12-04T04:53:51.822Z to 04/12/2020
export const getDateFromDateTime = (datetime: string): string => {
    const mDateTime = moment(datetime);
    return mDateTime.calendar(null, {
        sameDay: '[Today]',
        nextDay: '[Tomorrow]',
        nextWeek: 'dddd',
        lastDay: '[Yesterday]',
        lastWeek: '[Last] dddd',
        sameElse: 'DD/MM/YYYY',
    });
};
