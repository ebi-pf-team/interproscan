import logging


def set_logger(logfile, level):
    handlers = list()
    log_formatter = logging.Formatter('%(asctime)s | %(module)-12s | %(levelname)-8s | %(message)s')

    if logfile:
        file_handler = logging.FileHandler(logfile, 'a')
        file_handler.setFormatter(log_formatter)
        handlers.append(file_handler)
    else:
        console_handler = logging.StreamHandler()
        console_handler.setFormatter(log_formatter)
        handlers.append(console_handler)

    logging.basicConfig(level=level,
                        format=log_formatter,
                        handlers=handlers)
